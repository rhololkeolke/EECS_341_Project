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

class Vendor(DeclarativeBase):
    __tablename__ = 'vendor'

    id = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False)
    street1 = Column(String(100), nullable=False)
    street2 = Column(String(100))
    city = Column(String(100), nullable=False)
    state = Column(String(2), nullable=False)
    zip = Column(Integer, nullable=False)

class VendorPhone(DeclarativeBase):
    __tablename__ = 'vendor_phone'
    __table_args__ = (
        CheckConstraint("phone LIKE '(___)___-____'"),
    )

    vendor_id = Column(Integer, ForeignKey('vendor.id'), nullable=False, primary_key=True)
    phone = Column(String(13), nullable=False, primary_key=True)

def db_connect():
    """
    Performs database connection using database settings from settings.py
    Returns sqlalchemy engine instance
    """
    return create_engine(URL(**DATABASE))

def create_tables(engine):
    DeclarativeBase.metadata.create_all(engine)

if __name__ == '__main__':
    import sys
    import csv

    if(len(sys.argv) < 2):
        print "Error: must supply vendor data file"
        sys.exit(1)

    engine = db_connect()
    create_tables(engine)
    
    Session = sessionmaker(bind=engine)
    session = Session()

    with open(sys.argv[1], 'r') as csvfile:
        vendor_reader = csv.reader(csvfile, delimiter=',')
        vendor_reader.next() # skip first line

        for row in vendor_reader:
            vendor_dict = {'name': row[0],
                           'street1': row[1],
                           'city': row[2],
                           'state': row[3],
                           'zip': row[4]}
            vendor = session.query(Vendor).filter_by(**vendor_dict).first()
            if vendor is None:
                print "Creating Vendor %s" % vendor_dict['name']
                vendor = Vendor(**vendor_dict)
                session.add(vendor)
                session.flush()
            else:
                print "Vendor %s already exists" % vendor_dict['name']

            vendor_phone = session.query(VendorPhone).filter_by(vendor_id=vendor.id,
                                                                phone=row[5]).first()
            if vendor_phone is None:
                print "Inserting phone for vendor %s" % vendor_dict['name']
                vendor_phone = VendorPhone(vendor_id=vendor.id,
                                           phone=row[5])
                session.add(vendor_phone)
            session.commit()
    session.close()