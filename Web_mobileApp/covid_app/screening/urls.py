from django.urls import path
from . import views

app_name = "screening"

urlpatterns = [

     # Acceuil screening
     path('login', views.LoginView.as_view(), name="login"),
     path('tables', views.tables, name="tables"),
     path('', views.index, name="home"),
     path('backOffice', views.index_back_office, name="home_back_office"),
     path('forgot-password', views.forgot_password,  name='forgot_password'),
     path('register', views.register, name='register'),
     path('save_citizens', views.save_citizens, name='save_citizens')
]