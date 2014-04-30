#!/usr/bin/python
from models import db_connect, create_tables, Store, Product, Vendor, VendorPurchase, Supplies, Brand, Stock
from sqlalchemy.orm import sessionmaker

from database_settings import DATABASE

import random
import datetime
import time
import string
import pydb

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
        for i, product in enumerate(products):
            stock = session.query(Stock).filter_by(store_id=store.id,
                                                   upc=product.upc).first()
            if stock is None:
                print "product %d in store %d has no stock entry" % (product.upc, store.id)
                continue
            if stock.amount > 3:
                print "product %d of %d in store %d is already stocked" % (i+1, len(products), store.id)
                continue

            print "Creating vendor purchase for product %d of %d in store %d" % (i+1, len(products), store.id)
            amount = max(4, random.gauss(30, 5))
            
            vendor_info = product_info[product.upc]['vendor_info']
            vendor = random.choice(vendor_info)
            purchase = VendorPurchase(store_id=store.id,
                                      vendor_id=vendor[0],
                                      upc=product.upc,
                                      purchase_date=todays_date,
                                      amount=amount,
                                      unit_price=vendor[1])
            session.add(purchase)
            session.commit()
    session.close()