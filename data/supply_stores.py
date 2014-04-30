#!/usr/bin/python
from models import db_connect, create_tables, Store, Product, Vendor, VendorPurchase, Supplies, Brand
from sqlalchemy.orm import sessionmaker

from database_settings import DATABASE

import random
import datetime
import time
import string
import pydb

def strTimeProp(start, end, format, prop):
    """Get a time at a proportion of a range of two formatted times.

    start and end should be strings specifying times formated in the
    given format (strftime-style), giving an interval [start, end].
    prop specifies how a proportion of the interval to be taken after
    start.  The returned time will be in the specified format.
    """

    stime = time.mktime(time.strptime(start, format))
    etime = time.mktime(time.strptime(end, format))

    ptime = stime + prop * (etime - stime)

    tstruct = time.localtime(ptime)
    return datetime.date(tstruct.tm_year, tstruct.tm_mon, tstruct.tm_mday)

def randomDate(start, end):
    return strTimeProp(start.strftime("%m/%d/%Y"), end.strftime("%m/%d/%Y"), '%m/%d/%Y', random.random())

if __name__ == '__main__':
    engine = db_connect()
    create_tables(engine)

    Session = sessionmaker(bind=engine)
    session = Session()

    todays_datetime = datetime.datetime.now()
    todays_date = datetime.date(todays_datetime.year,
                                todays_datetime.month,
                                todays_datetime.day)


    vendors = session.query(Vendor).all()
    # # make sure every brand as at least one vendor supplying it
    # brands = session.query(Brand).all()
    # for brand in brands:
    #     if session.query(Supplies).filter_by(brand_id=brand.id).count() == 0:
    #         print "Adding suppliers for brand %s" % brand.name
    #         brand_vendors = random.sample(vendors, random.randint(1, len(vendors)-1))
    #         for vendor in brand_vendors:
    #             supplies = Supplies(vendor_id=vendor.id,
    #                                 brand_id=brand.id)
    #             session.add(supplies)
    #     else:
    #         print "Brand %s already has at least one supplier" % brand.name
    #     session.commit()

    stores = session.query(Store).all()

    products = session.query(Product).all()

    # first create baseline price and ordering information to make the stores semi consistent
    product_info = {}
    for i, product in enumerate(products):
        print "Generating product info for product %d of %d" % (i+1, len(products))
        product_info[product.upc] = {}
        product_info[product.upc]['price'] = round(random.uniform(.5, 1)*float(product.unit_price), 2)
        product_info[product.upc]['amount'] = random.uniform(2, 5)
        product_vendors = session.query(Supplies).filter_by(brand_id=product.brand).all()
        vendor_info = []
        for product_vendor in product_vendors:
            vendor_info.append([product_vendor.vendor_id,
                                round(min(random.gauss(product_info[product.upc]['price'],
                                             product_info[product.upc]['price']*.1),
                                    float(product.unit_price)), 2)])
        product_info[product.upc]['vendor_info'] = vendor_info
    session.close()

    for store in stores:
        session = Session()
        ordered_products_results = session.execute('''
                                                   SELECT oi.upc, MIN(o.order_date) as first_order
                                                   FROM orders as o,
                                                        order_item as oi
                                                   WHERE o.id = oi.order_id AND
                                                         o.store_id = %d
                                                   GROUP BY oi.upc''' % store.id)
        ordered_products = [r for r in ordered_products_results]
        for i, ordered_product in enumerate(ordered_products):
            #check if product is already ordered
            purchases = session.query(VendorPurchase).filter_by(store_id=store.id,
                                                                upc=ordered_product.upc).first()
            if purchases is not None:
                print "Product %d at store %d already has purchases" % (ordered_product.upc, store.id)
                continue

            # create a series of orders for these products
            print "Creating vendor purchases for product %d of %d in store %d" % (i+1, len(ordered_products), store.id)
            num_purchases = random.randint(1, 15)
            purchase_dates = set()
            for n in range(num_purchases):
                purchase_dates.add(randomDate(ordered_product.first_order, todays_date))
            purchase_dates = list(purchase_dates)
            purchase_dates.sort()
            if purchase_dates[0] != ordered_product.first_order:
                purchase_dates.insert(0, ordered_product.first_order)
            if purchase_dates[-1] != todays_date:
                purchase_dates.append(todays_date)

            previously_purchased_amount = 0
            for j, purchase_date in enumerate(purchase_dates[:-1]):
                print "Purchsing on date %d of %d" % (j+1, len(purchase_dates)-1)

                amount_results = session.execute('''
                                                 SELECT SUM(quantity) as amount
                                                 FROM orders as o,
                                                      order_item as oi
                                                 WHERE o.id = oi.order_id AND
                                                       o.store_id = %d AND
                                                       oi.upc = %d AND
                                                       o.order_date < to_date('%s', 'MM/DD/YYYY')
                                                 ''' % (store.id, ordered_product.upc,
                                                        purchase_date.strftime('%m/%d/%Y')))
                previously_ordered_amount = [r for r in amount_results][0].amount or 0

                amount_results = session.execute('''
                                                SELECT SUM(quantity) as amount
                                                FROM orders as o,
                                                     return_item as ri
                                                WHERE o.id = ri.order_id AND
                                                      o.store_id = %d AND
                                                      ri.upc = %d AND
                                                      ri.return_date < to_date('%s', 'MM/DD/YYYY')
                                                ''' % (store.id, ordered_product.upc,
                                                       purchase_date.strftime('%m/%d/%Y')))
                previously_returned_amount = [r for r in amount_results][0].amount or 0
                

                amount_results = session.execute('''
                                                SELECT SUM(quantity) as amount
                                                FROM orders as o,
                                                     order_item as oi
                                                WHERE o.id = oi.order_id AND
                                                      o.store_id = %d AND
                                                      oi.upc = %d AND
                                                      o.order_date >= to_date('%s', 'MM/DD/YYYY') AND
                                                      o.order_date < to_date('%s', 'MM/DD/YYYY')
                                                ''' % (store.id, ordered_product.upc,
                                                       purchase_date.strftime('%m/%d/%Y'),
                                                       purchase_dates[j+1].strftime('%m/%d/%Y')))
                future_order_amount = [r for r in amount_results][0].amount or 0

                amount = future_order_amount - previously_purchased_amount + \
                         previously_ordered_amount - previously_returned_amount
                amount = max(amount, 0)

                if amount == 0:
                    continue
                if amount < product_info[ordered_product.upc]['amount']:
                    amount = product_info[ordered_product.upc]['amount']

                vendor_info = product_info[ordered_product.upc]['vendor_info']
                vendor = random.choice(vendor_info)
                purchase = VendorPurchase(store_id=store.id,
                                          vendor_id=vendor[0],
                                          upc=ordered_product.upc,
                                          purchase_date=purchase_date,
                                          amount=amount,
                                          unit_price=vendor[1])
                previously_purchased_amount = previously_purchased_amount + amount
                session.add(purchase)
            session.commit()

    
        not_ordered_products_results = session.execute('''
                                                       (SELECT p.upc
                                                       FROM product as p)
                                                       EXCEPT
                                                       (SELECT oi.upc
                                                       FROM orders as o,
                                                            order_item as oi
                                                       WHERE o.id = oi.order_id AND
                                                             o.store_id = %d
                                                       GROUP BY oi.upc)''' % store.id)
        not_ordered_products = [r for r in not_ordered_products_results]
        for i, not_ordered_product in enumerate(not_ordered_products):
            # check if purchases already exist
            purchases = session.query(VendorPurchase).filter_by(store_id=store.id,
                                                                    upc=not_ordered_product.upc).first()
            if purchases is not None:
                print "Product %d already has a purchase at store %d" % (not_ordered_product.upc, store.id)
                continue

            # create a single order for this product
            print "Creating a vendor purchase for product %d of %d in store %d" % (i+1, len(not_ordered_products), store.id)
            amount = product_info[not_ordered_product.upc]['amount']
            purchase_date = randomDate(store.opening_date, todays_date)
            vendor_info = product_info[not_ordered_product.upc]['vendor_info']
            vendor = random.choice(vendor_info)

            purchase = VendorPurchase(store_id=store.id,
                                      vendor_id=vendor[0],
                                      upc=not_ordered_product.upc,
                                      purchase_date=purchase_date,
                                      amount=amount,
                                      unit_price=vendor[1])
            session.add(purchase)
            session.commit()
        session.commit()
        session.close()
