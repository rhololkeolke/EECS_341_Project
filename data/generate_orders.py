#!/usr/bin/python

from sqlalchemy import create_engine, Column, BigInteger, Integer, Numeric, String, Text, Date, Time, DateTime, Sequence, CheckConstraint, ForeignKey, ForeignKeyConstraint
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.engine.url import URL
from sqlalchemy.orm import sessionmaker
from sqlalchemy import desc

from database_settings import DATABASE

import random
import datetime
import time
import string

DeclarativeBase = declarative_base()

def db_connect():
    """
    Performs database connection using database settings from settings.py
    Returns sqlalchemy engine instance
    """
    return create_engine(URL(**DATABASE))

def create_tables(engine):
    DeclarativeBase.metadata.create_all(engine)

class Customer(DeclarativeBase):
    """Sqlalchemy cusomter model"""
    __tablename__ = "customer"
    __table_args__ = (
        CheckConstraint("gender IN ('M', 'F')"),
        CheckConstraint("loyalty_points >= 0")
    )

    loyalty_number = Column(Integer, primary_key=True)
    first_name = Column(String(100))
    middle_initial = Column(String(1))
    last_name = Column(String(100))
    birthdate = Column(Date)
    gender = Column(String(1))
    join_date = Column(Date)
    loyalty_points = Column(Integer)

class Product(DeclarativeBase):
    __tablename__ = 'product'
    __table_args__ = (
        CheckConstraint("size in ('S', 'M', 'L')"),
        CheckConstraint('unit_price > 0')
    )

    upc = Column(BigInteger, primary_key=True)
    name = Column(String(100), nullable=False)
    desc = Column(Text, nullable=False)
    size = Column(String(1))
    brand = Column(Integer, ForeignKey('brand.id'))
    unit_price = Column(Numeric, nullable=False)

    def __repr__(self):
        return "%s(upc=%r, name=%r, brand=%r, price=%r)" % (self.__class__, self.upc, self.name, self.brand, self.unit_price)

class OrderItem(DeclarativeBase):
    __tablename__ = 'order_item'
    __table_args__ = (
        CheckConstraint('quantity > 0'),
        CheckConstraint('discount BETWEEN 0.0 AND 1.0')
    )

    order_id = Column(Integer, ForeignKey('orders.id'), nullable=False, primary_key=True)
    upc = Column(BigInteger, ForeignKey('product.upc'), nullable=False, primary_key=True)
    quantity = Column(Integer, nullable=False)
    discount = Column(Numeric, nullable=False)
    
class ReturnItem(DeclarativeBase):
    __tablename__ = 'return_item'
    __table_args__ = (
        CheckConstraint('quantity > 0'),
    )
    
    order_id = Column(Integer, ForeignKey('orders.id'),
                      nullable=False, primary_key=True)
    upc = Column(Integer, ForeignKey('product.upc'),
                 nullable=False, primary_key=True)
    return_date = Column(Date, nullable=False, primary_key=True)
    quantity = Column(Integer, nullable=False)


class Store(DeclarativeBase):
    __tablename__ = 'store'
    
    id = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False)
    opening_date = Column(Date, nullable=False)
    street1 = Column(String(100), nullable=False)
    street2 = Column(String(100))
    city = Column(String(100), nullable=False)
    state = Column(String(2), nullable=False)
    zip = Column(Integer, nullable=False)

class ShippingLocation(DeclarativeBase):
    __tablename__ = 'shipping_location'
    
    id = Column(Integer, primary_key=True)
    loyalty_number = Column(Integer, ForeignKey('customer.loyalty_number'), 
                            nullable=False, primary_key=True)
    name = Column(String(100), nullable=False)
    street1 = Column(String(100), nullable=False)
    street2 = Column(String(100))
    city = Column(String(100), nullable=False)
    state = Column(String(2), nullable=False)
    zip = Column(Integer, nullable=False)

class Orders(DeclarativeBase):
    __tablename__ = 'orders'
    __table_args__ = (
        CheckConstraint("(shipping_loc IS NOT NULL AND shipping_cost IS NOT NULL) OR (shipping_loc IS NULL AND (shipping_cost IS NULL OR shipping_cost = 0))"),
        ForeignKeyConstraint(['shipping_loc', 'loyalty_number'], ['shipping_location.id', 'shipping_location.loyalty_number'])
    )

    id = Column(Integer, primary_key=True)
    order_date = Column(DateTime, nullable=False)
    store_id = Column(Integer, ForeignKey('store.id'), nullable=False)
    loyalty_number = Column(Integer, ForeignKey('customer.loyalty_number'), nullable=False)
    payment_type = Column(String(100), nullable=False)
    shipping_loc = Column(Integer)
    shipping_cost = Column(Numeric)


class Stock(DeclarativeBase):
    __tablename__ = 'stock'
    __table_args__ = (
        CheckConstraint('amount >= 0'),
    )

    store_id = Column(Integer, ForeignKey('store.id'),
                      nullable=False, primary_key=True)
    upc = Column(BigInteger, ForeignKey('product.upc'),
                 nullable=False, primary_key=True)
    amount = Column(Integer, nullable=False)

class VendorPurchase(DeclarativeBase):
    __tablename__ = 'vendor_purchase'
    __table_args__ = (
        CheckConstraint('amount > 0'),
        CheckConstraint('unit_price > 0')
    )

    store_id = Column(Integer, ForeignKey('store.id'),
                      nullable=False, primary_key=True)
    vendor_id = Column(Integer, ForeignKey('vendor.id'),
                       nullable=False, primary_key=True)
    upc = Column(BigInteger, ForeignKey('product.upc'),
                 nullable=False, primary_key=True)
    purchase_date = Column(Date, nullable=False, primary_key=True)
    amount = Column(Integer, nullable=False)
    unit_price = Column(Numeric, nullable=False)

class Brand(DeclarativeBase):
    __tablename__ = 'brand'

    id = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False, unique=True)

    def __repr__(self):
        return "%s(id=%r, name=%r)" % (self.__class__, self.id, self.name)

class Vendor(DeclarativeBase):
    __tablename__ = 'vendor'

    id = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False)
    street1 = Column(String(100), nullable=False)
    street2 = Column(String(100))
    city = Column(String(100), nullable=False)
    state = Column(String(2), nullable=False)
    zip = Column(Integer, nullable=False)

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
        product_amounts_result = session.execute(' '.join(['SELECT upc, MIN(purchase_date) as first_order, SUM(amount) as total_amount',
                                                    'FROM vendor_purchase',
                                                    'WHERE store_id = %d' % store.id,
                                                    'GROUP BY upc']))

        # convert to a list of product amounts and upc
        product_amounts = {}
        min_date = None
        for product_amount_result in product_amounts_result:
            product_amounts[product_amount_result.upc] = [product_amount_result.first_order, product_amount_result.total_amount]
            if min_date is None or min_date > product_amount_result.first_order:
                min_date = product_amount_result.first_order

        # find all existing stock and orders for product and remove from product_amounts
        for upc in product_amounts:
            stock = session.query(Stock).filter_by(store_id=store.id, upc=upc).first()
            orders_result = session.execute(' '.join('SELECT SUM(oi.amount) as total_amount',
                                                     'FROM orders as o',
                                                     'order_item as oi',
                                                     'WHERE o.id = oi.order_id AND'
                                                     'oi.upc = %d AND' % upc,
                                                     'o.store_id = %d' % store.id))
            orders = [r.total_amount for r in orders_result]
            if orders[0] == None:
                orders = 0
            else:
                orders = orders[0]

            returns_result = session.execute(' '.join('SELECT SUM(ri.amount) as total_amount',
                                                      'FROM orders as o',
                                                      'return_item as ri',
                                                      'WHERE o.id = ri.order_id AND',
                                                      'ri.upc = %d AND ' % upc,
                                                      'o.store_id = %d' % store.id))
            returns = [r.total_amount for r in returns_result]
            if returns[0] == None:
                returns = 0
            else:
                returns = returns[0]
            product_amounts[upc][1] = product_amounts[upc][1] - stock.amount - orders + returns


        # create orders of random size until product amounts is an empty dictionary
        while len(product_amounts) > 0:
            # create a new order
            order = Orders(order_date=randomDate(min_date, todays_date, random.random()),
                          customer=random.choice(customers),
                          payment_method=random.choice(payment_types))

            # if this is the website then add a shipping location and cost
            if store.id == website.id:
                shipping_locs = session.query(ShippingLocation).filter_by(loyalty_number=customer.loyalty_number).all()
                order.shipping_loc = random.choice(shipping_locs)
                order.shipping_cost = random.gauss(10, 3)

             available_product_amounts_results = session.execute(' '.join(['SELECT upc, SUM(amount) as total_amount',
                                                                           'FROM vendor_purchase',
                                                                           'WHERE store_id = %d AND' % store.id,
                                                                           "purchase_date < to_date(%s, 'YYYY/MM/DD')" % order.order_date.strftime("%Y/%m/%d"),
                                                                           'GROUP BY upc']))

            available_product_amounts = {}
            for available_product_amounts_result in available_product_amounts_results:
                # find all orders and returns for the product prior to this orders date and adjust available product amounts
                orders_result = session.execute(' '.join('SELECT SUM(oi.amount) as total_amount',
                                                         'FROM orders as o',
                                                         'order_item as oi'
                                                         'WHERE o.id = oi.order_id AND'
                                                         "o.order_date < to_date(%s, 'YYYY/MM/DD') AND" % order.order_date.strftime("%Y/%m/%d"),
                                                         'oi.upc = %d AND' % upc,
                                                         'o.store_id = %d' % store.id))
                orders = [r.total_amount for r in orders_result]
                if orders[0] == None:
                    orders = 0
                else:
                    orders = orders[0]
                    
                returns_result = session.execute(' '.join('SELECT SUM(ri.amount) as total_amount',
                                                          'FROM orders os o',
                                                          'return_item as ri',
                                                          'WHERE o.id = ri.order_id AND'
                                                          "o.order_date < to_date(%s 'YYYY/MM/DD') AND" % order.order_date.strftime("%Y/%m/%d"),
                                                          'ri.upc = %d AND' upc,
                                                          'o.store_id = %d' % store.id))
                returns = [r.total_amount for r in returns_result]
                if returns[0] == None:
                    returns = 0
                else:
                    returns = returns[0]
                available_product_amounts[available_product_amounts_result.upc] = available_product_amounts.total_amount - orders + returns

            # if everything is sold out prior to this date
            if len(available_product_amount) == 0:
                continue
            
    session.close()