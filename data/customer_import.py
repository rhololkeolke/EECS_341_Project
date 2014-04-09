#!/usr/bin/python
from models import db_connect, create_tables, Customer, CustomerEmail, CustomerPhone, ShippingLocation
from sqlalchemy.orm import sessionmaker

from database_settings import DATABASE

import random
import datetime
import time
import string

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