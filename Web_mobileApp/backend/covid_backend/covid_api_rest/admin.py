from django.contrib import admin
from . import models

# Register your models here
admin.site.register(models.Doctor)
admin.site.register(models.Scout)
admin.site.register(models.Citizen)
admin.site.register(models.HasScreened)
admin.site.register(models.Message)
admin.site.register(models.Symptom)
admin.site.register(models.OnlineTest)
admin.site.register(models.HasConsulted)
admin.site.register(models.New)
