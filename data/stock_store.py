#!/usr/bin/python


from models import db_connect, create_tables, Store, Brand, Product, Vendor, VendorPurchase, Shelf, ProductLocation, Stock
from sqlalchemy.orm import sessionmaker
from sqlalchemy import desc

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

    stores = session.query(Store).all()
    # create shelves if needed
    for store in stores:
        if len(session.query(Shelf).filter_by(store_id=store.id).all()) == 0:
            print "Creating shelves for store %d" % store.id
            num_shelves = random.randint(50,100)
            for i in range(num_shelves):
                shelf = Shelf(store_id=store.id)
                session.add(shelf)
        else:
            print "Store %d already has shelves" % store.id
        session.commit()
    
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
                print "Warning: Invalid product history in database"
                amount = 0
            print "[%3.0f%%] Stocking %d of product %d at store %d" % (100*float(i_p)/num_p, amount, product.upc, store.id)
                
            stock = Stock(store_id=store.id,
                          upc=product.upc,
                          amount=amount)
            session.add(stock)

            # check if already on a shelf
            shelf_location_results = session.execute('''
                                                     SELECT COUNT(*) AS num
                                                     FROM shelf as s,
                                                          product_location as pl
                                                     WHERE s.store_id = %d AND
                                                          s.id = pl.shelf_id AND
                                                          pl.upc = %d''' % (store.id, stock.upc))
            shelf_locations = [r for r in shelf_location_results][0]
            
            if shelf_locations.num == 0:
                print "Placing product %d on shelves" % stock.upc
                shelves = session.query(Shelf).filter_by(store_id=store.id).all()
                chosen_shelves = random.sample(shelves, 3)
                percentage_assigned = 0
                for i in range(2):
                    percent = random.uniform(0, 1)
                    shelf_amount = int(stock.amount*percent)
                    if shelf_amount == 0:
                        continue
                    
                    if percent + percentage_assigned > 1:
                        percent = 1 - percentage_assigned
                        percentage_assigned = 1
                    else:
                        percentage_assigned = percentage_assigned + percent
                    product_loc = ProductLocation(shelf_id=chosen_shelves[i].id,
                                                  upc=stock.upc,
                                                  amount=shelf_amount)
                    session.add(product_loc)
                percent = 1 - percentage_assigned
                shelf_amount = int(stock.amount*percent)
                if shelf_amount <= 0:
                    continue

                product_loc = ProductLocation(shelf_id=chosen_shelves[2].id,
                                              upc=stock.upc,
                                              amount=shelf_amount)
                session.add(product_loc)
            else:
                print "Product already on shelves"
            session.commit()

    session.close()