#!/usr/bin/python
from models import db_connect, create_tables, Store, Product, Vendor, VendorPurchase, Supplies, Brand
from sqlalchemy.orm import sessionmaker

from database_settings import DATABASE

import random
import datetime
import time
import string

from bisect import bisect_left

def binary_search(a, x, lo=0, hi=None):   # can't use a to specify default for hi
    hi = hi if hi is not None else len(a) # hi defaults to len(a)   
    pos = bisect_left(a,x,lo,hi)          # find insertion position
    return (pos if pos != hi and a[pos] == x else -1) # don't walk off the end

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
    # make sure every brand as at least one vendor supplying it
    brands = session.query(Brand).all()
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

    product_info = {}
    for i, product in enumerate(products):
        print "Product info of %d of %d products" % (i+1, len(products))
        product_info[product.upc] = {}
        product_info[product.upc]['base_price'] = random.uniform(.5, 1)
        product_info[product.upc]['base_amount'] = random.uniform(2, 500)
        product_info[product.upc]['vendors'] = session.query(Supplies).filter_by(brand_id=product.brand).all()
        product_info[product.upc]['vendor_prices'] = []
        for product_vendor in product_info[product.upc]['vendors']:
            product_info[product.upc]['vendor_prices'].append(float(product.unit_price)*random.gauss(product_info[product.upc]['base_price'], .1))


    for store in stores:
        for p, product in enumerate(products):
            print "[%d] Product %d of %d" % (store.id, p+1, len(products))

            if session.query(VendorPurchase).filter_by(upc=product.upc).count() != 0:
                print "Purchases for this product at this store already exist"
                continue

            product_order_date_result = session.execute('''
                                                        SELECT min(o.order_date) as date
                                                        FROM orders as o,
                                                          order_item as oi
                                                        WHERE o.store_id = %d AND
                                                           o.id = oi.order_id AND
                                                           oi.upc = %d
                                                        ''' % (store.id, product.upc))
            product_order_dates = [r for r in product_order_date_result]

            if len(product_order_dates) < 1:
                print "Error found no orders for product %d" % product.upc
                continue
                                                        
            product_order_date = product_order_dates[0]
            

            first_purchase_date = randomDate(store.opening_date, product_order_date.date)
            purchase_dates = set()
            for i in range(random.randint(1, 50)):
                purchase_dates.add(randomDate(first_purchase_date, todays_date))
            purchase_dates = list(purchase_dates)
            purchase_dates.insert(0, first_purchase_date)
            purchase_dates.append(todays_date)


            for i, purchase_date in enumerate(purchase_dates):
                print "Purchase date %d of %d" % (i+1, len(purchase_dates))
                if i == len(purchase_dates)-1:
                    break
                vendor_index = random.randint(0, len(product_info[product.upc]['vendors'])-1)
                vendor = product_info[product.upc]['vendors'][vendor_index]
                vendor_price = product_info[product.upc]['vendor_prices'][vendor_index]

                # amount purchased in past
                amount_results = session.execute('''
                                                 SELECT sum(vp.amount)
                                                 FROM vendor_purchase as vp
                                                 WHERE vp.store_id = %d AND
                                                       vp.upc = %d AND
                                                       vp.purchase_date <= to_date('%s', 'MM/DD/YYYY')
                                                 ''' % (store.id, product.upc,
                                                        purchase_date.strftime('%m/%d/%YYYY')))
                past_purchased_amount = [r for r in amount_results][0][0] or 0
                # amount ordered in past
                amount_results = session.execute('''
                                                 SELECT sum(oi.quantity)
                                                 FROM orders as o,
                                                      order_item as oi
                                                 WHERE o.id = oi.order_id AND
                                                       o.store_id = %d AND
                                                       oi.upc = %d AND
                                                       o.order_date <= to_date('%s', 'MM/DD/YYYY')
                                                 ''' % (store.id, product.upc,
                                                        purchase_date.strftime('%m/%d/%YYYY')))
                past_ordered_amount = [r for r in amount_results][0][0] or 0
                # amount returned in past
                amount_results = session.execute('''
                                                 SELECT sum(ri.quantity)
                                                 FROM orders as o,
                                                      return_item as ri
                                                 WHERE o.id = ri.order_id AND
                                                       o.store_id = %d AND
                                                       ri.upc = %d AND
                                                       ri.return_date <= to_date('%s', 'MM/DD/YYYY')
                                                 ''' % (store.id, product.upc,
                                                        purchase_date.strftime('%m/%d/%YYYY')))
                past_returned_amount = [r for r in amount_results][0][0] or 0
                # amount to be ordered before next purchase
                amount_results = session.execute('''
                                                  SELECT sum(oi.quantity)
                                                  FROM orders as o,
                                                       order_item as oi
                                                  WHERE o.id = oi.order_id AND
                                                        o.store_id = %d AND
                                                        oi.upc = %d AND
                                                        o.order_date > to_date('%s', 'MM/DD/YYYY') AND
                                                        o.order_date <= to_date('%s', 'MM/DD/YYYY')
                                                  ''' % (store.id, product.upc,
                                                         purchase_date.strftime('%m/%d/%YYYY'),
                                                         purchase_dates[i+1].strftime('%m/%d/%YYYY')))
                amount_to_order = [r for r in amount_results][0][0] or 0
                
                amount = past_purchased_amount - past_ordered_amount + \
                         past_returned_amount - amount_to_order
                if amount < 0:
                    amount = -1*amount;
                amount = max(product_info[product.upc]['base_amount'], amount)

                vendor_purchase = VendorPurchase(store_id=store.id,
                                                 vendor_id=vendor.vendor_id,
                                                 upc=product.upc,
                                                 purchase_date=purchase_date,
                                                 amount=amount,
                                                 unit_price=vendor_price)
                session.add(vendor_purchase)
        
            session.commit()

    # products = session.query(Product).all()
    # for product_num, product in enumerate(products):
    #     print "Starting product %d of %d" % (product_num, len(products))
    #     # only add purchases for products with no purchases in the past
    #     if session.query(VendorPurchase).filter_by(upc=product.upc).count() == 0:
    #         print "No purchases for this product exist"
    #         # create a vendor price list
    #         base_price = random.uniform(.5, 1)
    #         base_amount = random.uniform(2, 500)
    #         product_vendors = session.query(Supplies).filter_by(brand_id=product.brand).all()
    #         vendor_prices = []
    #         for product_vendor in product_vendors:
    #             vendor_prices.append([product_vendor.vendor_id, 
    #                                   float(product.unit_price)*random.gauss(base_price, .1)])

    #         # add at least one purchase to the website from at least one supplier
    #         num_purchases = random.randint(1, 5)
    #         print "Adding %d purchase(s) to the website" % num_purchases
    #         for i in range(num_purchases):
    #             purchase_date = randomDate('01/01/2009', '04/01/2014', random.random())
    #             vendor_price = random.choice(vendor_prices)
    #             amount = random.gauss(base_amount, base_amount*.1)
    #             vendor_purchase = VendorPurchase(store_id=website.id,
    #                                              vendor_id=vendor_price[0],
    #                                              upc=product.upc,
    #                                              purchase_date=purchase_date,
    #                                              amount=amount,
    #                                              unit_price=vendor_price[1])
    #             session.add(vendor_purchase)
            
    #         # now pick a random number of stores and for each one do the same thing
    #         vendor_purchase_stores = random.sample(stores, random.randint(1, len(stores)-1))
    #         print "Adding purchases to %d stores" % len(vendor_purchase_stores)
    #         for vendor_purchase_store in vendor_purchase_stores:
    #             num_purchases = random.randint(1, 5)
    #             print "Adding %d purchases to store %d" % (num_purchases, vendor_purchase_store.id)
    #             for i in range(num_purchases):
    #                 purchase_date = randomDate('01/01/2000', '04/01/2014', random.random())
    #                 vendor_price = random.choice(vendor_prices)
    #                 amount = random.gauss(base_amount, base_amount*.1)
    #                 vendor_purchase = VendorPurchase(store_id=vendor_purchase_store.id,
    #                                                  vendor_id=vendor_price[0],
    #                                                  upc=product.upc,
    #                                                  purchase_date=purchase_date,
    #                                                  amount=amount,
    #                                                  unit_price=vendor_price[1])
    #                 session.add(vendor_purchase)
    #         session.commit()
    #     else:
    #         print "Purchases already exist for this product"

    session.close()
