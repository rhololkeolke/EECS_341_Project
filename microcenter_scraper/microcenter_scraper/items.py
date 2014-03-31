# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

from scrapy.item import Item, Field

class Product(Item):
    upc = Field()
    name = Field()
    desc = Field()
    brand = Field()
    price = Field()
    category = Field()