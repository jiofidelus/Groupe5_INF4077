from django.shortcuts import render, redirect
from django.views import View, generic
from django.contrib.auth.decorators import login_required
from django.contrib.auth import authenticate, login
from django.http import HttpResponseRedirect, HttpResponse
from django.urls import reverse
from .models import Citizen, Scout


# Create your views here.
# home
def index(request):
    return render(request, 'pages/index/index.html')


# home_office
@login_required
def index_back_office(request):
    return render(request, 'pages/backOffice/index.html')


# tables des citoyens dejà depisté
def tables(request):
    return render(request, 'pages/backOffice/tables.html')


def forgot_password(request):
    return render(request, 'pages/backOffice/forgot-password.html')


def register(request):
    return render(request, 'pages/backOffice/register.html')


class LoginView(View):

    def get(self, req):
        return render(req, 'pages/backOffice/login.html')

    def post(self, req):
        username = req.POST['username']
        password = req.POST['password']
        user = authenticate(username=username, password=password)
        if user is not None:
            login(req, user=user)
            return redirect('/backOffice')
        else:
            return render(req, template_name="pages/backOffice/login.html", context={
                'error': "Echec d'authentification!"
            })


def save_citizens(request):
    return render(request, 'pages/backOffice/save_citizens.html')
