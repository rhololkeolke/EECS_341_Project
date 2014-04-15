#!/usr/bin/python
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

if __name__ == '__main__':
    engine = db_connect()
    create_tables(engine)
    
    Session = sessionmaker(bind=engine)
    session = Session()

    todays_datetime = datetime.datetime.now()
    todays_date = datetime.date(todays_datetime.year,
                                todays_datetime.month,
                                todays_datetime.day)

    # get all customers who have their first order less than the store's opening date
    first_order_results = session.execute('''WITH min_order_date(loyalty_number, date) AS
                                                  (SELECT o.loyalty_number, min(o.order_date)
                                                   FROM orders as o
                                                   GROUP BY o.loyalty_number),
                                                  first_order(loyalty_number, id) AS
                                                  (SELECT o.loyalty_number, min(o.id)
                                                   FROM orders as o,
                                                        min_order_date as mod
                                                   WHERE o.loyalty_number = mod.loyalty_number AND
                                                         o.order_date = mod.date
                                                   GROUP BY o.loyalty_number)
                                             SELECT o.loyalty_number, o.id, s.opening_date
                                             FROM orders as o,
                                                  store as s,
                                                  first_order as fo
                                             WHERE o.id = fo.id AND
                                                   s.id = o.store_id AND
                                                   o.order_date < s.opening_date
                                             ORDER BY o.loyalty_number''')
    for first_order in first_order_results:
        order = session.query(Orders).filter_by(id=first_order.id)
        order.order_date = first_order.opening_date
        session.add(order)
        session.commit()

    bad_orders = session.exeucte('''
                                 SELECT o.id, c.join_date
                                 FROM orders as o,
                                      customer as c
                                 WHERE o.loyalty_number = c.loyalty_number AND
                                       o.order_date < c.join_date''')
    for bad_order in bad_orders:
        order = session.query(Orders).filter_by(id=bad_order.id)
        order.order_date = randomDate(bad_order.join_date, todays_date)
        session.add(order)
        session.commit()
        
    bad_orders = session.execute('''
                                 SELECT o.id, s.opening_date
                                 FROM orders as o,
                                      store as s
                                 WHERE o.store_id = s.id AND
                                       o.order_date < s.opening_date''')
    for bad_order in bad_orders:
        order = session.query(Orders).filter_by(id=bad_order.id)
        order.order_date = randomDate(bad_order.opening_date, todays_date)
        session.add(order)
        session.commit()

    session.close()