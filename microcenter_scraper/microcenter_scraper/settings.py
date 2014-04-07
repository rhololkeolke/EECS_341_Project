# Scrapy settings for microcenter_scraper project
#
# For simplicity, this file contains only the most important settings by
# default. All the other settings are documented here:
#
#     http://doc.scrapy.org/en/latest/topics/settings.html
#

BOT_NAME = 'microcenter_scraper'

SPIDER_MODULES = ['microcenter_scraper.spiders']
NEWSPIDER_MODULE = 'microcenter_scraper.spiders'

# Crawl responsibly by identifying yourself (and your website) on the user-agent
#USER_AGENT = 'microcenter_scraper (+http://www.yourdomain.com)'

from database_settings import DATABASE
