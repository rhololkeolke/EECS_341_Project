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

if __name__ == '__main__':
    engine = db_connect()
    create_tables(engine)

    Session = sessionmaker(bind=engine)
    session = Session()

    all_customers = session.query(Customer).all()
    for customer in all_customers:
        work_shipping_loc = session.query(ShippingLocation).filter_by(loyalty_number=customer.loyalty_number,
                                                                      name='Work').first()
        if work_shipping_loc is not None:
            print "Customer %d already has work shipping location" % customer.loyalty_number
            continue;
            
        print "Creating work shipping location"

        existing_shipping_locs = session.query(ShippingLocation).filter('loyalty_number != %d' % customer.loyalty_number).all()
        home_shipping_loc = random.choice(existing_shipping_locs)
        work_shipping_loc = ShippingLocation(loyalty_number=customer.loyalty_number,
                                             name='Work',
                                             street1=home_shipping_loc.street1,
                                             street2=home_shipping_loc.street2,
                                             city=home_shipping_loc.city,
                                             state=home_shipping_loc.state,
                                             zip=home_shipping_loc.zip)
        session.add(work_shipping_loc)
        session.commit()
    session.close()

    