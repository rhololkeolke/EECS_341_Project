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
        product['name'] = map(strip, sel.xpath("//div[@itemprop='name']/text()").extract())
        product['desc'] = map(strip, sel.xpath("//div[@itemprop='description']//*[not(self::script)]//text()").extract())
        price_values = map(strip, sel.xpath("//span[@itemprop='price']/text()").extract() + \
                       sel.xpath("//span[@itemprop='price']/span[last()]/text()").extract())
        product['price'] = ['.'.join(price_values)]
        
        category_link = sel.xpath('//div[@id="product-details-control"]/h1/small/a[position()>1]/text()').extract()
        product['category'] = category_link
        print "category_link", category_link
        product['brand'] = map(strip, sel.xpath('//small[@itemprop="brand"]/a/text()').extract())
        product['upc'] = map(strip, sel.xpath('//div[@id="detail-list"]/dl/dd[last()]/text()').extract())
        return product
