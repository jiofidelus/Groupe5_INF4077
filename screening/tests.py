from unittest import skipUnless

import selenium
from django.conf import settings
from django_selenium_test import SeleniumTestCase, PageElement
from selenium.webdriver.common.by import By


@skipUnless(getattr(settings, 'SELENIUM_WEBDRIVERS', False),
            "Selenium is unconfigured")
class HelloTestCase(SeleniumTestCase):
    heading_earth = PageElement(By.ID, 'earth')
    heading_world = PageElement(By.ID, 'world')
    button = PageElement(By.CSS_SELECTOR, 'button')

    def test_toggle(self):
        # Visit the page
        self.selenium.get(self.live_server_url)

        # Check that the earth heading is visible
        self.assertTrue(self.heading_earth.is_displayed())
        self.assertFalse(self.heading_world.is_displayed())

        # Toggle and check the new condition
        self.button.click()
        self.heading_world.wait_until_is_displayed()
        self.assertFalse(self.heading_earth.is_displayed())
        self.assertTrue(self.heading_world.is_displayed())

        # Toggle again and re-check
        self.button.click()
        self.heading_earth.wait_until_is_displayed()
        self.assertTrue(self.heading_earth.is_displayed())
        self.assertFalse(self.heading_world.is_displayed())
