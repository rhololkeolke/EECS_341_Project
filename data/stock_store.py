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

class Brand(DeclarativeBase):
    __tablename__ = 'brand'

    id = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False, unique=True)

    def __repr__(self):
        return "%s(id=%r, name=%r)" % (self.__class__, self.id, self.name)

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

class Shelf(DeclarativeBase):
    __tablename__ = 'shelf'
    
    id = Column(Integer, nullable=False, primary_key=True)
    store_id = Column(Integer, ForeignKey('store.id'), nullable=False)

class ProductLocation(DeclarativeBase):
    __tablename__ = 'product_location'
    __table_args__ = (
        CheckConstraint('amount > 0'),
    )

    shelf_id = Column(Integer, ForeignKey('shelf.id'), 
                      nullable=False, primary_key=True)
    upc = Column(BigInteger, ForeignKey('product.upc'),
                 nullable=False, primary_key=True)
    amount = Column(Integer, nullable=False)

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
        product_purchases = session.execute('SELECT upc, MAX(purchase_date) as purchase_date \
                                            FROM vendor_purchase \
                                            WHERE store_id = %d \
                                            GROUP BY upc' % store.id)
        for product_purchase in product_purchases:
            # check if already stocked
            stock = session.query(Stock).filter_by(store_id=store.id,
                                                   upc=product_purchase.upc).first()
            purchased_amount = session.query(VendorPurchase).filter_by(store_id=store.id,
                                                                      upc=product_purchase.upc,
                                                                      purchase_date=product_purchase.purchase_date).first().amount
            if stock is None:
                print "No stock for %d" % product_purchase.upc
                
                stock = Stock(store_id=store.id,
                              upc=product_purchase.upc,
                              amount=int(purchased_amount*random.uniform(.1, 1)))
                session.add(stock)
            else:
                print "product %d is already stocked" % product_purchase.upc

            # check if already on a shelf
            shelf_locations = session.execute(' '.join(['SELECT COUNT(*) AS num',
                                                        'FROM shelf as s,',
                                                        'product_location as pl',
                                                        'WHERE s.store_id = %d AND' % store.id,
                                                        's.id = pl.shelf_id AND',
                                                        'pl.upc = %d' % stock.upc])).first()
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