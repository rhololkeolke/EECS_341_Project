#!/usr/bin/python
from models import db_connect, create_tables, Customer, Product, OrderItem, ReturnItem, Store, ShippingLocation, Orders, Stock, VendorPurchase, Brand, Vendor
from sqlalchemy.orm import sessionmaker
from sqlalchemy import desc

from database_settings import DATABASE

import random
import datetime
import time
import string

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

def generate_order_products(order, products):
    # select a random amount of random products for this order
    order_products = random.sample(products, random.randint(1, 20))
    order_amounts = {}
    for i, order_product in enumerate(order_products):
        print "\t\tGenerating product %d of %d in order" % (i+1, len(order_products))
        quantity = random.randint(1, 20)
        order_amounts[order_product.upc] = quantity
        discount = round(max(random.gauss(0, .1), 0), 2)
        order_item = OrderItem(order_id=order.id,
                               upc=order_product.upc,
                               quantity=quantity,
                               discount=discount)
        session.add(order_item)

    # for some probability return some items
    if random.uniform(0, 1) < .05:
        print "\t\tCustomer is returning items"
        returned_products = random.sample(order_products, random.randint(1, len(order_products)))
        for i, returned_product in enumerate(returned_products):
            print "\t\t\tGenerating return %d of %d" % (i+1, len(returned_products))
            quantity = random.randint(1, order_amounts[returned_product.upc])
            return_date = randomDate(order.order_date,
                                     order.order_date + datetime.timedelta(days=14))
            return_item = ReturnItem(order_id=order.id,
                                     upc=returned_product.upc,
                                     return_date=return_date,
                                     quantity=quantity)
            session.add(return_item)
    session.flush()

if __name__ == '__main__':
    engine = db_connect()
    create_tables(engine)

    Session = sessionmaker(bind=engine)
    session = Session()

    # for each store get a list of amount of every product purchased
    todays_datetime = datetime.datetime.now()
    todays_date = datetime.date(todays_datetime.year,
                                todays_datetime.month,
                                todays_datetime.day)

    payment_types = ['visa', 'mastercard', 'american express', 'discover', 'cash', 'check', 'money order', 'gift card']

    stores = session.query(Store).filter("name != 'Website'").all()
    website = session.query(Store).filter_by(name='Website').first()

    customers = session.query(Customer).all()
    products = session.query(Product).all()

    for cust_index, customer in enumerate(customers):
        print "starting customer %d of %d" % (cust_index+1, len(customers))
        # see if customer already has a first order
        # if so assume that orders have already been generated for this customer
        # so skip
        first_order = session.query(Orders).filter_by(order_date=customer.join_date,
                                                      loyalty_number=customer.loyalty_number).first()
        if first_order is not None:
            print "customer %d already has orders" % (cust_index+1)
            continue

        customer_store = random.choice(stores)
        shipping_locs = session.query(ShippingLocation).filter_by(loyalty_number=customer.loyalty_number).all()
        
        print "\tGenerating first order"
        first_order_store = None
        if random.uniform(0,1) > .5:
            first_order_store = website
        else:
            first_order_store = customer_store

        first_order = Orders(order_date=customer.join_date,
                             store_id=first_order_store.id,
                             loyalty_number=customer.loyalty_number,
                             payment_type=random.choice(payment_types))
        if first_order_store.name == 'Website':
            first_order.shipping_loc = random.choice(shipping_locs).id
            first_order.shipping_cost = round(random.uniform(5, 20), 2)

        session.add(first_order)
        session.flush()

        generate_order_products(first_order, products)
                       
        num_orders = random.randint(0, 50)
        last_order_date = customer.join_date
        for i in range(num_orders):
            print "\tGenerating order %d of %d" % (i+1, num_orders)
            # choose the order for the store
            order_store = customer_store
            # if the customer's first order was online
            # make it more probable that they are ordering online again
            website_prob = None
            if first_order_store.name == 'Website':
                website_prob = .9
            else:
                website_prob = .1

            if random.uniform(0, 1) < website_prob:
                order_store = website

            order_date = randomDate(last_order_date, todays_date)
            order = Orders(order_date=order_date,
                           store_id=order_store.id,
                           loyalty_number=customer.loyalty_number,
                           payment_type=random.choice(payment_types))
            if order_store.name == 'Website':
                order.shipping_loc = random.choice(shipping_locs).id
                order.shipping_cost = round(random.uniform(5, 20), 2)

            session.add(order)
            session.flush()

            generate_order_products(order, products)

        session.commit()
