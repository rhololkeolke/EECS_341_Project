from scrapy.contrib.spiders import CrawlSpider, Rule
from scrapy.contrib.linkextractors.sgml import SgmlLinkExtractor
from scrapy.selector import Selector
from scrapy.http import Request

from string import strip

from microcenter_scraper.items import Product

class MicrocenterProductSpider(CrawlSpider):
    name = "products"
    allowed_domains = ["microcenter.com"]
    start_urls = [
        "http://www.microcenter.com"
    ]
    rules = [
        Rule(SgmlLinkExtractor(allow=['/category/[\d]+,?[\d]*/[a-zA-Z]+'])),
        Rule(SgmlLinkExtractor(allow=['/product/[\d]+/.*']), callback='parse_product')
    ]

    def parse_product(self, response):
        sel = Selector(response)
        product = Product()
        product['name'] = ' '.join(map(strip, sel.xpath("//div[@itemprop='name']/text()").extract()))
        product['desc'] = '\n'.join(map(strip, sel.xpath("//div[@itemprop='description']//*[not(self::script)]//text()").extract()))
        price_values = map(strip, sel.xpath("//span[@itemprop='price']/text()").extract() + \
                       sel.xpath("//span[@itemprop='price']/span[last()]/text()").extract())
        try:
            product['price'] = float('.'.join(price_values))
        except:
            print "ERROR float('.'.join(price_values)): ", '.'.join(price_values)
            return
            
        
        category_link = sel.xpath('//div[@id="product-details-control"]/h1/small/a[position()>1]/text()').extract()
        product['category'] = category_link

        product['brand'] = ' '.join(map(strip, sel.xpath('//small[@itemprop="brand"]/a/text()').extract()))
        try:
            product['upc'] = int(''.join(map(strip, sel.xpath('//div[@id="detail-list"]/dl/dd[last()]/text()').extract())))
        except:
            print """ERROR int(''.join(map(strip, sel.xpath('//div[@id="detail-list"]/dl/dd[last()]/text()').extract()))): """, ''.join(map(strip, sel.xpath('//div[@id="detail-list"]/dl/dd[last()]/text()').extract()))
            return

        return product
