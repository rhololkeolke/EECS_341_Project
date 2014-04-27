#!/usr/bin/python
from models import db_connect, create_tables, Store, Brand, Product, Vendor, VendorPurchase, Shelf, ProductLocation, Stock
from sqlalchemy.orm import sessionmaker
from sqlalchemy import desc

from database_settings import DATABASE

import random
import datetime
import time
import string

if __name__ == '__main__':
    engine = db_connect()
    create_tables(engine)
    
    Session = sessionmaker(bind=engine)
    session = Session()

    stores = session.query(Store).all()

    for store in stores:
        print "Stocking store %d" % store.id
        product_results = session.execute('''
                                          SELECT DISTINCT upc
                                          FROM vendor_purchase
                                          WHERE store_id = %d
                                          ORDER BY upc''' % store.id)
        products = [r for r in product_results]
        num_p = len(products)
        for i_p, product in enumerate(products):
            # check if already stocked
            stock = session.query(Stock).filter_by(store_id=store.id,
                                                   upc=product.upc).first()
            if stock is not None:
                print "[%3.0f%%] Product %d already stocked at store %d" % (100*float(i_p)/num_p, product.upc, store.id)
                continue

            purchased_amount_results = session.execute('''
                                                       SELECT SUM(amount) as amount
                                                       FROM vendor_purchase
                                                       WHERE store_id = %d AND
                                                             upc = %d''' % (store.id, product.upc))
            purchased_amount = [r for r in purchased_amount_results][0].amount or 0

            ordered_amount_results = session.execute('''
                                                     SELECT SUM(quantity) as amount
                                                     FROM orders AS o,
                                                          order_item AS oi
                                                     WHERE o.id = oi.order_id AND
                                                           o.store_id = %d AND
                                                           oi.upc = %d''' % (store.id, product.upc))
            ordered_amount = [r for r in ordered_amount_results][0].amount or 0

            returned_amount_results = session.execute('''
                                                      SELECT SUM(quantity) as amount
                                                      FROM orders AS o,
                                                           return_item AS ri
                                                      WHERE o.id = ri.order_id AND
                                                            o.store_id = %d AND
                                                            ri.upc = %d''' % (store.id, product.upc))
            returned_amount = [r for r in returned_amount_results][0].amount or 0

            amount = purchased_amount - ordered_amount + returned_amount
            if amount < 0:
                print "Found invalid product history in database"
                
                purchase = session.query(VendorPurchase).filter_by(store_id=store.id).filter_by(upc=product.upc).update({VendorPurchase.amount: VendorPurchase.amount+1})
                session.commit()
    session.close()
                                          
                