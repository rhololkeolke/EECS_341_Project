from scrapy.contrib.spiders import CrawlSpider, Rule
from scrapy.contrib.linkextractors.sgml import SgmlLinkExtractor
from scrapy.selector import Selector
from scrapy.http import Request

from microcenter_scraper.items import Product

class MicrocenterProductSpider(CrawlSpider):
    name = "products"
    allowed_domains = ["microcenter.com"]
    start_urls = [
        "http://www.microcenter.com"
    ]
    rules = [Rule(SgmlLinkExtractor(allow=['/category/[\d]+,?[\d]*/[a-zA-Z]+']), callback='parse_page')]

    def parse_page(self, response):
        sel = Selector(response)
        title = sel.xpath('//title/text()').extract()
        print title
            