#!/usr/bin/python

from sqlalchemy import create_engine, Column, BigInteger, Integer, Numeric, String, Text, Date, Time, DateTime, Sequence, CheckConstraint, ForeignKey, ForeignKeyConstraint
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.engine.url import URL
from sqlalchemy.orm import sessionmaker

from database_settings import DATABASE

import random
import datetime
import time
import string

DeclarativeBase = declarative_base()

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


def db_connect():
    """
    Performs database connection using database settings from settings.py
    Returns sqlalchemy engine instance
    """
    return create_engine(URL(**DATABASE))

def create_tables(engine):
    DeclarativeBase.metadata.create_all(engine)

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

class Vendor(DeclarativeBase):
    __tablename__ = 'vendor'

    id = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False)
    street1 = Column(String(100), nullable=False)
    street2 = Column(String(100))
    city = Column(String(100), nullable=False)
    state = Column(String(2), nullable=False)
    zip = Column(Integer, nullable=False)

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

class Supplies(DeclarativeBase):
    __tablename__ = 'supplies'
    
    vendor_id = Column(Integer, ForeignKey('vendor.id'),
                       nullable=False, primary_key=True)
    brand_id = Column(Integer, ForeignKey('brand.id'),
                      nullable=False, primary_key=True)

class Brand(DeclarativeBase):
    __tablename__ = 'brand'

    id = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False, unique=True)

    def __repr__(self):
        return "%s(id=%r, name=%r)" % (self.__class__, self.id, self.name)


if __name__ == '__main__':
    engine = db_connect()
    create_tables(engine)

    Session = sessionmaker(bind=engine)
    session = Session()

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

    website = session.query(Store).filter_by(name='Website').first()
    if website is None:
        raise Exception("Website is not in the store relation")
    stores = session.query(Store).filter('id != %d' % website.id).all()

    products = session.query(Product).all()
    for product_num, product in enumerate(products):
        print "Starting product %d of %d" % (product_num, len(products))
        # only add purchases for products with no purchases in the past
        if session.query(VendorPurchase).filter_by(upc=product.upc).count() == 0:
            print "No purchases for this product exist"
            # create a vendor price list
            base_price = random.uniform(.5, 1)
            base_amount = random.uniform(2, 500)
            product_vendors = session.query(Supplies).filter_by(brand_id=product.brand).all()
            vendor_prices = []
            for product_vendor in product_vendors:
                vendor_prices.append([product_vendor.vendor_id, 
                                      float(product.unit_price)*random.gauss(base_price, .1)])

            # add at least one purchase to the website from at least one supplier
            num_purchases = random.randint(1, 5)
            print "Adding %d purchase(s) to the website" % num_purchases
            for i in range(num_purchases):
                purchase_date = randomDate('01/01/2009', '04/01/2014', random.random())
                vendor_price = random.choice(vendor_prices)
                amount = random.gauss(base_amount, base_amount*.1)
                vendor_purchase = VendorPurchase(store_id=website.id,
                                                 vendor_id=vendor_price[0],
                                                 upc=product.upc,
                                                 purchase_date=purchase_date,
                                                 amount=amount,
                                                 unit_price=vendor_price[1])
                session.add(vendor_purchase)
            
            # now pick a random number of stores and for each one do the same thing
            vendor_purchase_stores = random.sample(stores, random.randint(1, len(stores)-1))
            print "Adding purchases to %d stores" % len(vendor_purchase_stores)
            for vendor_purchase_store in vendor_purchase_stores:
                num_purchases = random.randint(1, 5)
                print "Adding %d purchases to store %d" % (num_purchases, vendor_purchase_store.id)
                for i in range(num_purchases):
                    purchase_date = randomDate('01/01/2000', '04/01/2014', random.random())
                    vendor_price = random.choice(vendor_prices)
                    amount = random.gauss(base_amount, base_amount*.1)
                    vendor_purchase = VendorPurchase(store_id=vendor_purchase_store.id,
                                                     vendor_id=vendor_price[0],
                                                     upc=product.upc,
                                                     purchase_date=purchase_date,
                                                     amount=amount,
                                                     unit_price=vendor_price[1])
                    session.add(vendor_purchase)
            session.commit()
        else:
            print "Purchases already exist for this product"

    session.close()
