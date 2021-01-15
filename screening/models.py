from django.db import models
from django.contrib.auth.models import AbstractUser
from django.urls import reverse
from django.utils import timezone
from covid_app import settings

from datetime import date, timedelta
from dateutil.relativedelta import relativedelta

from django.contrib.auth.models import (BaseUserManager, AbstractBaseUser)


class MyUserManager(BaseUserManager):
    def create_user(self, username, is_doctor, is_scout, mobile_phone_working, first_name, last_name, password=None):
        """
        Creates and saves a User with the given email, date of
        birth and password.
        """
        if not mobile_phone_working:
            raise ValueError('doctor/scout must have mobile phone')

        user = self.model(
            username=username,
            mobile_phone_working=mobile_phone_working,
            is_doctor=is_doctor,
            is_scout=is_scout,
            first_name=first_name,
            last_name=last_name
        )
        user.set_password(password)
        user.save(using=self._db)
        return user

    def create_superuser(self, username, is_doctor, is_scout, mobile_phone_working, first_name, last_name,
                         password=None):
        """
        Creates and saves a superuser with the given email, date of
        birth and password.
        """
        user = self.create_user(
            username=username,
            password=password,
            mobile_phone_working=mobile_phone_working,
            is_doctor=is_doctor,
            is_scout=is_scout,
            first_name=first_name,
            last_name=last_name
        )
        user.is_superuser = True
        user.get_all_permissions()
        user.is_staff = True
        user.save(using=self._db)
        return user


# Create your models here.
class Citizen(models.Model):
    first_name = models.CharField(max_length=30, default="")
    second_name = models.CharField(max_length=30, blank=True, default="")
    birth_day = models.DateField()
    gender = models.CharField(max_length=1, choices=[('M', 'M'), ('F', 'F')], null=False, default="M")
    identity_card_id = models.CharField(max_length=255, unique=True, default=0)
    nationality = models.CharField(max_length=255, choices=[
        ('Cmr', 'Cameroonian'),
        ('Oth', 'Others')
    ], default="Cameroonian")
    mobile_phone = models.CharField(null=False, blank=False, unique=True, max_length=20, default=0)
    picture = models.ImageField(upload_to="citizens_pictures", blank=True, null=True)
    register_date = models.DateTimeField(auto_now_add=True)
    age = models.IntegerField(default=0)

    def get_absolute_url(self):
        return reverse('screening:has_screened-list')

    """def __str__(self):
        return "{name: {} {} , id_card: {} , mobile_phone: {]}".format(self.first_name , self.second_name , self.identity_card_id, 
                                                                       self.mobile_phone)"""

    @property
    def get_age(self):
        return abs(date.today().year - self.birth_day.year)

    def save(self, *args, **kwargs):
        self.age = self.get_age
        super(Citizen, self).save(*args, **kwargs)

    def __str__(self):
        return "name:{} {} , id_card: {}, phone: {}  ".format(self.first_name, self.second_name,
                                                                                     self.identity_card_id,
                                                                                     self.mobile_phone)

    class Meta:
        db_table = "citizens"


class User(AbstractUser):
    mobile_phone_working = models.IntegerField(null=True, blank=True, unique=True)
    is_doctor = models.BooleanField(default=False)
    is_scout = models.BooleanField(default=False)
    REQUIRED_FIELDS = ['is_doctor', 'is_scout', 'first_name', 'last_name', 'mobile_phone_working']
    citizens_screened = models.ManyToManyField(Citizen, through='HasScreened')
    objects = MyUserManager()

    class Meta:
        db_table = "users"


class HasScreened(models.Model):
    scout_who_screened = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE,
                                           related_name="scout_info_tracker",
                                           null=True)
    citizen_who_has_been_screened = models.ForeignKey(Citizen, on_delete=models.CASCADE,
                                                      related_name="citizen_who_has_been_screened")
    status = models.CharField(choices=[
        ('+', '+'),
        ('-', '-'),
        ('?', '?'),
        ('r', 'r'),
        ('d', 'd')
    ], max_length=1)
    screening_date = models.DateTimeField(auto_now=True)
    type_screening = models.CharField(choices=[
        ('quick', 'quick'),
        ('slow', 'slow')
    ], max_length=5)
    region = models.CharField(choices={
        ('Centre', 'Centre'),
        ('West', 'West'),
        ('North', 'North'),
        ('North_West', 'North_West'),
        ('East', 'East'),
        ('South', 'South'),
        ('South_West', 'South_West'),
        ('Littoral', 'Littoral'),
        ('Adamaoua', 'Adamaoua'),
        ('Extreme_North', 'Extreme_North')

    }, max_length=13)

    department = models.CharField(max_length=25)
    quarter = models.CharField(max_length=25)
    city = models.CharField(max_length=25)
    site_following_him = models.CharField(max_length=255, blank=True, null=True)
    screening_date_date = models.DateField(auto_now=True)

    import datetime
    @property
    def get_screening_date_date(self):
        if self.screening_date:
            dob = self.screening_date
            return dob.date
        else:
            return ''

    def save(self, *args, **kwargs):
        self.screening_date_date = self.get_screening_date_date
        super(HasScreened, self).save(*args, **kwargs)

    class Meta:
        db_table = "has_screened"


""""
class Message(models.Model):
    id_message = models.AutoField(primary_key=True)
    # media = models.FileField(upload_to="media", null=True)
    user_has_written = models.ForeignKey(settings.AUTH_USER_MODEL, null=True, on_delete=models.CASCADE,
                                         related_name='user_has_written')
    to_user = models.ForeignKey(Citizen, null=True, on_delete=models.CASCADE, related_name='to_user')
    date_send = models.DateTimeField()
    message = models.TextField()

    class Meta:
        db_table = "messages" """


class Symptom(models.Model):
    symptom_id = models.AutoField(primary_key=True)
    title = models.CharField(max_length=55, default="")
    text_explain = models.CharField(max_length=2048, default="")
    illustration = models.FileField(upload_to="media", null=True)
    value = models.IntegerField()
    register_date = models.DateTimeField(auto_now=True)

    def __str__(self):
        return self.text_explain

    class Meta:
        db_table = "symptoms"


class OnlineTest(models.Model):
    id_test = models.AutoField(primary_key=True)
    percentage = models.IntegerField()
    comments = models.CharField(max_length=1024, default="")
    result = models.CharField(max_length=1024, default="")
    date_test = models.DateTimeField(auto_now=True)
    citizen_who_has_been_tested = models.ForeignKey(Citizen, null=True, on_delete=models.CASCADE)
    state = models.TextField(null=True, blank=True)
    voice = models.FileField(upload_to="voices", null=True)
    symptoms_detected = models.ManyToManyField(Symptom, related_name="online_tests")

    class Meta:
        db_table = "online_tests"


class HasConsulted(models.Model):
    doctor_who_has_consulted = models.ForeignKey(settings.AUTH_USER_MODEL, null=True, on_delete=models.CASCADE)
    citizen_has_been_consulted = models.ForeignKey(Citizen, null=True, on_delete=models.CASCADE)
    date_consulted = models.DateField()
    doctor_observation = models.CharField(max_length=2048, default="")

    class Meta:
        db_table = "has_consulted"


class New(models.Model):
    title = models.CharField(max_length=255, default="")
    body = models.TextField(default="")
    image = models.FileField(upload_to="media/news")
    user_has_posted = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE, null=True)
