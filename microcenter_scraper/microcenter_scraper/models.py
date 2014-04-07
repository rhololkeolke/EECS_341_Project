from sqlalchemy import create_engine, Column, BigInteger, Integer, Numeric, String, Text, Date, Time, DateTime, Sequence, CheckConstraint, ForeignKey, ForeignKeyConstraint
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.engine.url import URL

import settings

DeclarativeBase = declarative_base()

def db_connect():
    """
    Performs database connection using database settings from settings.py
    Returns sqlalchemy engine instance
    """
    return create_engine(URL(**settings.DATABASE))

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
    street1 = Column(String(100))
    street2 = Column(String(100))
    city = Column(String(100))
    state = Column(String(2))
    zip = Column(Integer)

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

class StorePhone(DeclarativeBase):
    __tablename__ = 'store_phone'
    __table_args__ = (
        CheckConstraint("phone LIKE '(___)___-____'"),
    )

    store_id = Column(Integer, ForeignKey('store.id'), nullable=False, primary_key=True)
    phone = Column(String, nullable= False, primary_key=True)

class ShippingLocation(DeclarativeBase):
    __tablename__ = 'shipping_location'
    
    id = Column(Integer, primary_key=True)
    loyalty_number = Column(Integer, ForeignKey('customer.loyalty_number'), 
                            nullable=False, primary_key=True)
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

class Brand(DeclarativeBase):
    __tablename__ = 'brand'

    id = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False)

class Product(DeclarativeBase):
    __tablename__ = 'product'
    __table_args__ = (
        CheckConstraint("size in ('S', 'M', 'L')"),
    )

    upc = Column(BigInteger, primary_key=True)
    name = Column(String(100), nullable=False)
    description = Column(Text, nullable=False)
    size = Column(String(1))
    brand = Column(Integer, ForeignKey('brand.id'))

class ProductTypeTree(DeclarativeBase):
    __tablename__ = 'product_type_tree'
    __table_args__ = (
        CheckConstraint('lft > 0'),
        CheckConstraint('rgt > 0'),
        CheckConstraint('lft < rgt')
    )

    type = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False)
    lft = Column(Integer, nullable=False)
    rgt = Column(Integer, nullable=True)

class ProductType(DeclarativeBase):
    __tablename__ = 'product_type'
    
    upc = Column(BigInteger, ForeignKey('product.upc'), nullable=False, primary_key=True)
    id = Column(Integer, ForeignKey('product_type_tree.type'), nullable=False, primary_key=True)

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

class Stock(DeclarativeBase):
    __tablename__ = 'stock'
    __table_args__ = (
        CheckConstraint('amount >= 0'),
        CheckConstraint('unit_price > 0')
    )

    store_id = Column(Integer, ForeignKey('store.id'),
                      nullable=False, primary_key=True)
    upc = Column(BigInteger, ForeignKey('product.upc'),
                 nullable=False, primary_key=True)
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

class ProductSpec(DeclarativeBase):
    __tablename__ = 'product_spec'
    
    upc = Column(BigInteger, ForeignKey('product.upc'),
                 nullable=False, primary_key=True)
    desc = Column(String(100), nullable=False, primary_key=True)
    value = Column(String(100), nullable=False, primary_key=True)

class Supplies(DeclarativeBase):
    __tablename__ = 'supplies'
    
    vendor_id = Column(Integer, ForeignKey('vendor.id'),
                       nullable=False, primary_key=True)
    brand_id = Column(Integer, ForeignKey('brand.id'),
                      nullable=False, primary_key=True)
    
class StoreClosing(DeclarativeBase):
    __tablename__ = 'store_closing'
    
    store_id = Column(Integer, ForeignKey('store.id'),
                      nullable=False, primary_key=True)
    closed_date = Column(Date, nullable=False, primary_key=True)
    desc = Column(String(100))

class StoreHours(DeclarativeBase):
    __tablename__ = 'store_hours'
    __table_args__ = (
        CheckConstraint("day_of_week IN ('M', 'Tu', 'W', 'Th', 'F', 'Sa', 'Su')"),
    )

    store_id = Column(Integer, ForeignKey('store.id'),
                      nullable=False, primary_key=True)
    day_of_week = Column(String(2), nullable=False, primary_key=True)
    open_hour = Column(Time, nullable=False)
    close_hour = Column(Time, nullable=False)
