from sqlalchemy import create_engine, Column, BigInteger, Integer, Numeric, String, Text, Date, Time, DateTime, Sequence, CheckConstraint, ForeignKey, ForeignKeyConstraint
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.engine.url import URL
from sqlalchemy.exc import IntegrityError

from database_settings import DATABASE

DeclarativeBase = declarative_base()

def db_connect():
    """
    Performs database connection using database settings from settings.py
    Returns sqlalchemy engine instance
    """
    return create_engine(URL(**DATABASE))

def create_tables(engine):
    DeclarativeBase.metadata.create_all(engine)

def get_or_create(session, model, **kwargs):
    instance = None
    try:
        instance = session.query(model).filter_by(**kwargs).first()
        if instance is None:
            instance = model(**kwargs)
            session.add(instance)
            session.flush()
    except:
        session.rollback()
        session.close()
        raise
    return instance

class User(DeclarativeBase):
    """Sqlalchemy user model"""
    __tablename__ = "user"
    __table_args__ = (
        CheckConstraint("role in ('customer', 'employee', 'DBA')"),
        CheckConstraint("email LIKE '_%@_%._%'"),
        CheckConstraint("(role = 'customer' AND loyalty_number IS NOT NULL) OR (role != 'customer' AND loyalty_number IS NULL)")
    )
    username = Column(String(100), primary_key=True)
    email = Column(String(100), nullable=False)
    password = Column(String(100), nullable=False)
    salt = Column(String(100), nullable=False)
    role = Column(String(30), default='customer')
    loyalty_number = Column(Integer, ForeignKey('customer.loyalty_number'),
                            nullable=True)

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

class Brand(DeclarativeBase):
    __tablename__ = 'brand'

    id = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False, unique=True)

    def __repr__(self):
        return "%s(id=%r, name=%r)" % (self.__class__, self.id, self.name)

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

class ProductTypeTree(DeclarativeBase):
    __tablename__ = 'product_type_tree'
    __table_args__ = (
        CheckConstraint('lft >= 0'),
        CheckConstraint('rgt > 0'),
        CheckConstraint('lft < rgt')
    )

    id = Column(Integer, primary_key=True)
    name = Column(String(100), unique=True, nullable=False)
    lft = Column(Integer, nullable=False)
    rgt = Column(Integer, nullable=True)

    def __repr__(self):
        return "%s(id=%r, name=%r, lft=%r, rgt=%r)" % (self.__class__, self.id, self.name, self.lft, self.rgt)

class ProductType(DeclarativeBase):
    __tablename__ = 'product_type'
    
    upc = Column(BigInteger, ForeignKey('product.upc'), nullable=False, primary_key=True)
    id = Column(Integer, ForeignKey('product_type_tree.id'), nullable=False, primary_key=True)

    def __repr__(self):
        return "%s(upc=%r, id=%r)" % (self.__class__, self.upc, self.id)

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
    )

    store_id = Column(Integer, ForeignKey('store.id'),
                      nullable=False, primary_key=True)
    upc = Column(BigInteger, ForeignKey('product.upc'),
                 nullable=False, primary_key=True)
    amount = Column(Integer, nullable=False)


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
    value = Column(Text, nullable=False, primary_key=True)

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

def insert_product_type(session, nodes):
    parent_row = session.query(ProductTypeTree).filter_by(lft=0).first()
    rows = [parent_row]
    for curr_node in nodes:
        curr_row = session.query(ProductTypeTree).filter_by(name=curr_node).first()
        if curr_row is None:
            children = session.execute("SELECT  hc.name, hc.lft, hc.rgt \
                                       FROM    product_type_tree hp \
                                       JOIN    product_type_tree hc \
                                       ON      hc.lft BETWEEN hp.lft AND hp.rgt \
                                       WHERE   hp.id = %d \
                                       AND \
                                       ( \
                                       SELECT  COUNT(*) \
                                       FROM    product_type_tree hn \
                                       WHERE   hc.lft BETWEEN hn.lft AND hn.rgt \
                                       AND hn.lft BETWEEN hp.lft AND hp.rgt \
                                       ) = (SELECT COUNT(*) \
                                       FROM product_type_tree hn \
                                       WHERE hc.lft BETWEEN hn.lft AND hn.rgt \
                                       AND hn.lft BETWEEN hp.lft AND hp.rgt AND hn.id = %d )+1" % ((parent_row.id,)*2))
            if children.rowcount == 0:
                rgts_to_update = session.query(ProductTypeTree).filter(ProductTypeTree.rgt > parent_row.lft).update({ProductTypeTree.rgt: ProductTypeTree.rgt+2})

                lfts_to_update = session.query(ProductTypeTree).filter(ProductTypeTree.lft > parent_row.lft).update({ProductTypeTree.lft: ProductTypeTree.lft+2})

                curr_row = ProductTypeTree(name=curr_node, lft=parent_row.lft+1, rgt=parent_row.lft+2)
            else:
                max_right = max([x.rgt for x in children])
                rgts_to_update = session.query(ProductTypeTree).filter(ProductTypeTree.rgt > max_right).update({ProductTypeTree.rgt: ProductTypeTree.rgt + 2})

                lfts_to_update = session.query(ProductTypeTree).filter(ProductTypeTree.lft > max_right).update({ProductTypeTree.lft: ProductTypeTree.lft + 2})

                curr_row = ProductTypeTree(name=curr_node, lft=max_right+1, rgt=max_right+2)
            session.add(curr_row)
            session.flush()
        rows.append(curr_row)
        parent_row = curr_row
    return rows
