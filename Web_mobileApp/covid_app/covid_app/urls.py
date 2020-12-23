
from django.urls import path,include

from . import views


app_name = "statistiques"

urlpatterns = [
    path('', views.index, name='index'),
]
