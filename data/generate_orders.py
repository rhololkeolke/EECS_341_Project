#!/usr/bin/python
from models import db_connect, create_table, Customer, Product, OrderItem, ReturnItem, Store, ShippingLocation, Orders, Stock, VendorPurchase, Brand, Vendor
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

    return time.strftime(format, time.localtime(ptime))


def randomDate(start, end, prop):
    return strTimeProp(start, end, '%m/%d/%Y', prop)

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

    stores = session.query(Store).all()
    website = session.query(Store).filter_by(name='Website').first()
    if website is None:
        raise Exception("Could not find website store")
    customers = session.query(Customer).all()
    for store in stores:
        # get all product's oldest order date as well as total amount ordered over time
        product_amounts_results = session.execute(' '.join(['SELECT upc, MIN(purchase_date) as first_order, SUM(amount) as total_amount',
                                                            'FROM vendor_purchase',
                                                            'WHERE store_id = %d' % store.id,
                                                            'GROUP BY upc']))

        # turn the results into a list of easier access
        product_amounts_result = [r for r in product_amounts_results]

        # construct a list of sorted minimum dates
        oldest_order_dates = [r.first_order for r in product_amounts_result]
        oldest_order_dates.sort()
        oldest_order_dates.append(todays_date)

        # for each date construct the total amount of product available up until that time
        # the indices of product available will match minimum dates and the elements will be dicts of upc to product amounts
        product_available_by_date = []
        for order_date in oldest_order_dates:
            product_avail_results = session.execute(' '.join('SELECT upc, SUM(amount) as total_amount',
                                                                 'FROM vendor_purchase',
                                                                 'WHERE store_id = %d AND' % store.id,
                                                                 "purchase_date < to_date(%s, 'YYYY/MM/DD')" % order_date.strftime('%Y/%m/%d'),
                                                                 'group by upc'))
            product_available = {}
            for product_avail_row in product_avail_results:
                product_available[product_avail_row.upc] = product_avail_row.total_amount
            product_available_by_date.append(product_available)
        
        # subtract off what is in stock from the last element
        stock = session.query(Stock).filter_by(store_id=store.id).all()
        for stocked_product in stock:
            product_available_by_date[-1][stocked_product.upc] = product_available_by_date[-1][stocked_product.upc] - stocked_product.amount

        # go through and remove items that have already been ordered
        orders = session.query(Orders).filter_by(store_id=store.id).order_by(Orders.order_date).all()
        product_avail_diff = [{}]*len(orders)
        i = 0
        for order in orders:
            # find the index for this order
            while oldest_order_dates[i] < order.order_date:
                i = i + 1
                if i >= len(olest_order_dates):
                    i = len(oldest_order_dates)
                    break
            i = i - 1 # back off one to get the latest oldest date less than order date
            
            # for each product in the order update the diff list
            order_products = session.query(OrderItem).filter_by(order_id=order.id).all()
            for order_product in order_products:
                product_avail_diff[i][order_product.upc] = product_avail_diff[i][order_product.upc] + order_product.quantity
                
            return_items = session.query(ReturnItem).filter_by(order_id=order.id).order_by(ReturnItem.return_date).all()
            j = i
            for return_item in return_items:
                # find the index for this return
                while oldest_order_dates[j] < return_item.return_date:
                    j = j +1
                    if j >= len(oldest_order_dates):
                        j = len(oldest_order_dates)
                        break
                j = j - 1 # back off one to get the latest oldest date less than return date
                
            
            
    session.close()