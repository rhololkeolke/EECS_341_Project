# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: http://doc.scrapy.org/en/latest/topics/item-pipeline.html

from sqlalchemy.orm import sessionmaker
from models import db_connect, create_tables, get_or_create, insert_product_type, Product, Brand, ProductType, ProductTypeTree, ProductSpec

import random

class MicrocenterProductPipeline(object):
    def __init__(self):
        engine = db_connect()
        create_tables(engine)
        self.Session = sessionmaker(bind=engine)

        session = self.Session()

        # make sure the root of the product type tree exists
        ptt = session.query(ProductTypeTree).filter_by(lft=0).first()
        if ptt is None:
            ptt = ProductTypeTree(name='Root', lft=0, rgt=1)
            try:
                session.add(ptt)
                session.commit()
            except:
                session.rollback()
                raise
            finally:
                session.close()

            

    def process_item(self, item, spider):
        session = self.Session()

        brand = session.query(Brand).filter_by(name=item['brand']).first()
        if brand is None:
            print "Creating new brand"
            brand = Brand(name=item['brand'])
            session.add(brand)
            session.flush()
        else:
            print "Brand existed"

        categories = insert_product_type(session, item['category'])

        product = session.query(Product).filter_by(upc=item['upc']).first()
        if product is None:
            print "Creating new product"
            product = Product(upc=item['upc'],
                              name=item['name'],
                              desc=item['desc'],
                              brand=brand.id,
                              unit_price=item['price'])
            session.add(product)
            session.flush()
        else:
            print "Product exists"

        if len(categories) > 0:
            product_type = session.query(ProductType).filter_by(upc=product.upc, id=categories[-1].id).first()
            if product_type is None:
                print "Creating product type"
                product_type = ProductType(upc=product.upc, id=categories[-1].id)
                session.add(product_type)
                session.flush()
            else:
                print "Product type exists"

        # add specifications for the product
        print "len(specs):", len(item['specs'])
        for spec in item['specs']:
            product_spec = session.query(ProductSpec).filter_by(upc=product.upc,
                                                                desc=spec[0],
                                                                value=spec[1]).first()
            if product_spec is None:
                product_spec = ProductSpec(upc=product.upc,
                                           desc=spec[0],
                                           value=spec[1])
                session.add(product_spec)
                session.flush()

        session.commit()
        session.close()
        return item
