from django.db import models
from django.contrib.auth.models import User
from django.utils import timezone


# Create your models here.
class Citizen(models.Model):
    first_name = models.CharField(max_length=30, default="")
    second_name = models.CharField(max_length=30, blank=True, default="")
    birth_day = models.DateField()
    gender = models.CharField(max_length=1, choices=[('M', 'M'), ('F', 'F')], null=False, default="M")
    identity_card_id = models.CharField(max_length=255, unique=True, null=True, blank=True)
    nationality = models.CharField(max_length=255, choices=[
        ('Cmr', 'Cameroonian'),
        ('Oth', 'Others')
    ], default="Cameroonian")
    mobile_phone = models.IntegerField(null=False, blank=False, unique=True)
    picture = models.ImageField(upload_to="media/citizens_pictures")
    register_date = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "citizens"


class Doctor(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, null=True)
    grade = models.CharField(choices=[
        ('Specialist', 'Specialist'),
        ('Generalist', 'Generalist')
    ], max_length=10)
    speciality = models.CharField(max_length=55, null=True)

    # Numero de telephone de travail
    mobile_phone_working = models.IntegerField(null=False, blank=False, unique=True)

    class Meta:
        db_table = 'doctors'


class Scout(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, null=True)
    type = models.CharField(choices=[
        ('FreeWorker', 'FreeWorker'),
        ('Epidemiologist', 'Epidemiologist'),
        ('Doctor', 'Doctor')
    ], max_length=14)

    # Numero de telephone de travail
    mobile_phone_working = models.IntegerField(null=False, blank=False, unique=True)

    citizens_tracked = models.ManyToManyField(Citizen, through='HasScreened', related_name="citizens_tracked")

    class Meta:
        db_table = 'scouts'


class HasScreened(models.Model):
    scout_who_screened = models.ForeignKey(Scout, on_delete=models.CASCADE, related_name="scout_info_tracker",
                                           null=True)
    citizen_who_has_been_screened = models.ForeignKey(Citizen, on_delete=models.CASCADE,
                                                      related_name="citizen_who_has_been_screened")
    status = models.CharField(choices=[
        ('+', '+'),
        ('-', '-'),
        ('?', '?')
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
    site_following_him = models.CharField(max_length=255)

    class Meta:
        db_table = "has_screened"


class Message(models.Model):
    id_message = models.AutoField(primary_key=True)
    media = models.FileField(upload_to="media", null=True)
    user_has_written = models.ForeignKey(User, null=True, on_delete=models.CASCADE, related_name='user_has_written')
    to_user = models.ForeignKey(User, null=True, on_delete=models.CASCADE, related_name='to_user')
    date_send = models.DateTimeField()

    class Meta:
        db_table = "messages"


class Symptom(models.Model):
    symptom_id = models.AutoField(primary_key=True)
    title = models.CharField(max_length=55, default="")
    text_explain = models.CharField(max_length=2048, default="")
    illustration = models.FileField(upload_to="media", null=True)
    value = models.IntegerField()
    register_date = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "symptoms"


class OnlineTest(models.Model):
    id_test = models.AutoField(primary_key=True)
    percentage = models.IntegerField()
    comments = models.CharField(max_length=1024, default="")
    result = models.CharField(max_length=1024, default="")
    date_test = models.DateTimeField(auto_now=True)
    citizen_who_has_been_tested = models.ForeignKey(Citizen, null=True, on_delete=models.CASCADE)
    symptoms_detected = models.ManyToManyField(Symptom, related_name="online_tests")

    class Meta:
        db_table = "online_tests"


class HasConsulted(models.Model):
    doctor_who_has_consulted = models.ForeignKey(Doctor, null=True, on_delete=models.CASCADE)
    citizen_has_been_consulted = models.ForeignKey(Citizen, null=True, on_delete=models.CASCADE)
    date_consulted = models.DateField()
    doctor_observation = models.CharField(max_length=2048, default="")

    class Meta:
        db_table = "has_consulted"


class New(models.Model):
    title = models.CharField(max_length=255, default="")
    body = models.TextField(default="")
    image = models.FileField(upload_to="media/news")
    user_has_posted = models.ForeignKey(User, on_delete=models.CASCADE, null=True)
