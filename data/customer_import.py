#!/usr/bin/python
from sqlalchemy import create_engine, Column, BigInteger, Integer, Numeric, String, Text, Date, Time, DateTime, Sequence, CheckConstraint, ForeignKey, ForeignKeyConstraint
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.engine.url import URL
from sqlalchemy.orm import sessionmaker

from database_settings import DATABASE

import random
import datetime
import time

DeclarativeBase = declarative_base()

import random
import string
import time

email_domains = ['gmail.com', 'yahoo.com', 'hotmail.com', 'aol.com']

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

class CustomerEmail(DeclarativeBase):
    __tablename__ = 'customer_email'
    __table_args__ = (
        CheckConstraint("email LIKE '_%@_%._%'"),
    )

    loyalty_number = Column(Integer, ForeignKey('customer.loyalty_number'), 
                            nullable=False, primary_key=True)
    email = Column(String, primary_key=True, nullable=False)

class CustomerPhone(DeclarativeBase):
    __tablename__ = 'customer_phone'
    __table_args__ = (
        CheckConstraint("phone LIKE '(___)___-____'"),
    )

    loyalty_number = Column(Integer, ForeignKey('customer.loyalty_number'),
                            nullable=False, primary_key=True)
    phone = Column(String(13), primary_key=True, nullable=False)

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
    import sys
    import csv

    if(len(sys.argv) < 2):
        print "Error: Must supply with customer data file"
        sys.exit(1)

    engine = db_connect()
    create_tables(engine)

    Session = sessionmaker(bind=engine)
    session = Session()
    
    with open(sys.argv[1], 'r') as csvfile:
        customer_reader = csv.reader(csvfile, delimiter=',')
        customer_reader.next() # skip the first row
        for row in customer_reader:
            todays_datetime = datetime.datetime.now()
            todays_date = datetime.date(todays_datetime.year,
                                        todays_datetime.month,
                                        todays_datetime.day)
            gender = 'M'
            if row[0] == 'female':
                gender = 'F'
            cust_dict = {'first_name': row[1],
                         'middle_initial': row[2],
                         'last_name': row[3],
                         'birthdate': datetime.date(*time.strptime(row[10], '%m/%d/%Y')[:3]),
                         'gender': gender}
            customer = session.query(Customer).filter_by(**cust_dict).first()
            if customer is None:
                print "Creating customer"
                customer = Customer(**cust_dict)
                customer.loyalty_points = random.randint(0, 2000)
                customer.join_date = randomDate(row[10], 
                                                 todays_date.strftime('%m/%d/%Y'),
                                                 random.random())
                session.add(customer)
                session.flush()
            else:
                print "Customer exists"

            # add default shipping location of home
            shipping_loc = session.query(ShippingLocation).filter_by(loyalty_number=customer.loyalty_number,
                                                                     name='Home').first()
            if shipping_loc is None:
                print "Creating shipping location"
                shipping_dict = {'street1': row[4],
                                 'city': row[5],
                                 'state': row[6],
                                 'zip': row[7]}
                shipping_loc = ShippingLocation(**shipping_dict)
                shipping_loc.loyalty_number = customer.loyalty_number
                shipping_loc.name = 'Home'
                                                
                session.add(shipping_loc)
                session.flush()
            else:
                print "Shipping location exists"
            
            customer_email = session.query(CustomerEmail).filter_by(loyalty_number=customer.loyalty_number,
                                                               email=row[8]).first()
            if customer_email is None:
                print "Creating customer email"
                customer_email = CustomerEmail(loyalty_number=customer.loyalty_number,
                                               email=row[8])
                session.add(customer_email)
                session.flush()
            else:
                print "Customer email exists"
            
            # generate a random number of random emails
            while random.uniform(0, 1) > .5:
                print "Generating another random email address"
                email_str = ''.join([random.choice(string.letters) for x in range(7)]) + '@' + \
                            random.choice(email_domains)
                customer_email_rand = CustomerEmail(loyalty_number=customer.loyalty_number,
                                                    email=email_str)
                session.add(customer_email_rand)

            # generate a random number of random phone numbers
            while random.uniform(0, 1) > .5:
                print "Generating a random phone number"
                phone_str = '(%d)%d-%d' % (random.randint(100,999),
                                              random.randint(100,999),
                                              random.randint(1000,9999))
                customer_phone_rand = CustomerPhone(loyalty_number=customer.loyalty_number,
                                                    phone=phone_str)
                session.add(customer_phone_rand)

            print "Committing changes"
            session.commit()
    session.close()