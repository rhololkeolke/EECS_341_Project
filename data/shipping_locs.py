#!/usr/bin/python

from models import db_connect, create_tables, Customer, ShippingLocation
from sqlalchemy.orm import sessionmaker

from database_settings import DATABASE

import random
import datetime
import time
import string

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

    