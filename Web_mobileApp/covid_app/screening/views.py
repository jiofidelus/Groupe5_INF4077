from django.http import HttpResponse
from django.shortcuts import render, redirect
from django.urls import reverse_lazy
from django.views import View, generic
from django.contrib.auth.decorators import login_required
from django.contrib.auth import authenticate, login
from django.db import IntegrityError
from .models import Citizen, HasScreened
from . import forms
from bootstrap_modal_forms.generic import BSModalCreateView, BSModalReadView, BSModalUpdateView, BSModalDeleteView
from twilio.rest import Client
from covid_app import settings
from . import models


# Create your views here.
class PersonalMessageView(View):
    has_screened = None

    def get(self, request):
        self.has_screened = HasScreened.objects.filter(scout_who_screened=request.user)
        return render(request, 'message_box/index.html', context={'has_screened': self.has_screened})

    def post(self, request):
        self.has_screened = HasScreened.objects.filter(scout_who_screened=request.user)
        errors = {}
        success = ""
        message = str(request.POST['message'])
        if message.rstrip().__len__() == 0:
            errors['message'] = "Votre message est vide"
        else:
            citizen = Citizen.objects.get(id=request.POST['to'])
            client = Client(settings.TWILIO_ACCOUNT_SID, settings.TWILIO_AUTH_TOKEN)
            try:
                client.messages.create(
                    to=citizen.mobile_phone,
                    from_=settings.TWILIO_NUMBER,
                    body=message
                )

                message = None
                success = "Message bien achéminé!!"
            except Exception:
                errors['numero'] = Exception.__cause__

        return render(request, template_name="message_box/index.html", context={
            'errors': errors, 'success': success, 'has_screened': self.has_screened
            , 'message': message
        })


class CitizenCreateModalView(BSModalCreateView):
    template_name = "pages/backOffice/citizen_createModal.html"
    form_class = forms.CitizenModelForm
    success_message = "Citizen was successful create!"
    success_url = reverse_lazy('screening:citizens-list')

    class Meta:
        model = Citizen
        fields = "__all__"


class OnlineTestListView(generic.ListView):
    model = models.OnlineTest
    paginate_by = 100
    queryset = models.OnlineTest.objects.all()
    template_name = "pages/backOffice/online_tests_list.html"


class HasScreenedCreateModalView(BSModalCreateView):
    template_name = "pages/backOffice/has_screened_createModal.html"
    form_class = forms.HasScreenedModelForm
    success_message = "the citizen was successful screened"
    success_url = reverse_lazy('screening:citizens-list')


class HasScreenedReadModalView(BSModalReadView):
    model = HasScreened
    template_name = "pages/backOffice/has_screened_read.html"
    fields = "__all__"


class HasScreenedDeleteModalView(BSModalDeleteView):
    template_name = "pages/backOffice/has_screened_delete.html"
    success_url = reverse_lazy('screening:has_screened-list')
    model = HasScreened
    success_message = "screening was delete!"


class HasScreenedUpdateModalView(BSModalUpdateView):
    template_name = "pages/backOffice/has_screened_update.html"
    success_url = reverse_lazy('screening:has_screened-list')
    form_class = forms.HasScreenedModelForm
    success_message = "screening was update!"
    model = HasScreened


class HasScreenedListView(generic.ListView):
    template_name = "pages/backOffice/citizens_list.html"
    paginate_by = 100
    model = HasScreened

    def get_queryset(self):
        user = self.request.user
        return HasScreened.objects.filter(scout_who_screened=user)


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


# Vue du home
def index(request):
    return render(request, 'pages/index/index.html')


def map_index(request):
    return render(request, 'pages/backOffice/map.html')


class OnlineTestView(View):

    def get(self, request):
        symptoms = models.Symptom.objects.all()
        form = forms.OnlineTestForm()
        form.set_data()
        return render(request, 'form_test/index.html', context={
            'symptoms': symptoms, 'form': form
        })

    def post(self, request):
        symptoms = models.Symptom.objects.all()
        form = forms.OnlineTestForm(request.POST, request.FILES)
        form.set_data()
        integrity_error = None
        if form.is_valid():
            first_name = form.cleaned_data['first_name']
            second_name = form.cleaned_data['second_name']
            birth_day = form.cleaned_data['birth_day']
            gender = form.cleaned_data['gender']
            mobile_phone = form.cleaned_data['mobile_phone']
            nationality = form.cleaned_data['nationality']
            id_card = form.cleaned_data['identity_card_id']
            symptoms = form.cleaned_data['symptoms']
            state = form.cleaned_data['state']
            voice = form.cleaned_data['voice']

            try:
                citizen = Citizen.objects.create(first_name=first_name, second_name=second_name, birth_day=birth_day,
                                                 identity_card_id=id_card, gender=gender, mobile_phone=mobile_phone,
                                                 nationality=nationality)
                online_test = models.OnlineTest(citizen_who_has_been_tested=citizen, state=state, voice=voice,
                                                percentage=25, comments="Vous ne risquez pas grand chose! Restez "
                                                                        "juste confinez")
                online_test.save()
                for sy in symptoms:
                    online_test.symptoms_detected.add(sy)

                return HttpResponse("Bien enregistré!! Nous vous communiquerons vos resultats d'ici peu!!")

            except IntegrityError as ex:
                integrity_error = ex.__str__()

        return render(request, 'form_test/index.html', context={
            'form': form, 'integrity_error': integrity_error
        })


class LoginView(View):

    def get(self, req):
        return render(req, 'pages/backOffice/login.html')

    def post(self, request):
        username = request.POST['username']
        password = request.POST['password']
        user = authenticate(username=username, password=password)

        if user is not None:
            login(request, user=user)
            return redirect('/backOffice')
        else:
            return render(request, template_name="pages/backOffice/login.html", context={
                'error': "Echec d'authentification!"
            })
