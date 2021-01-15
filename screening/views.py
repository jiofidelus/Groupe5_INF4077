from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import render, redirect
from django.urls import reverse_lazy
from django.views import View, generic
from django.contrib.auth.decorators import login_required
from django.contrib.auth import authenticate, login
from django.db import IntegrityError
from django_ajax.decorators import ajax

from . import forms
from bootstrap_modal_forms.generic import BSModalCreateView, BSModalReadView, BSModalUpdateView, BSModalDeleteView
from twilio.rest import Client
from covid_app import settings
from . import models

import json

from django.db.models import Count, Q
from django.http import JsonResponse
from django.core import serializers

from screening.models import Citizen
from screening.models import HasScreened
from django.core.serializers.json import DjangoJSONEncoder


# Create your views here.
class PersonalMessageView(View):

    def get(self, request):
        return render(request, 'pages/backOffice/messages.html')

    def post(self, request):
        errors = {}
        success = ""
        message = str(request.POST['message'])
        number = str(request.POST['number'])
        if message.rstrip().__len__() == 0:
            errors['message'] = "Votre message est vide"
        else:
            client = Client(settings.TWILIO_ACCOUNT_SID, settings.TWILIO_AUTH_TOKEN)
            try:
                client.messages.create(
                    to=number,
                    from_=settings.TWILIO_NUMBER,
                    body=message
                )

                message = None
                success = "Message bien achéminé!!"
            except Exception:
                errors['numero'] = Exception.__cause__

        return render(request, template_name="message_box/index.html", context={
            'errors': errors, 'success': success, 'message': message
        })


@ajax
def save_citizen(request):
    form = forms.CitizenModelForm2(request.POST, request.FILES)
    was_save = False
    if form.is_valid():
        form.save(commit=True)
        was_save = True
    return {"errors": form.errors, "was_save": was_save}


@ajax
def save_screening(request):
    form = forms.HasScreenedModelForm2(request.POST, request.FILES)
    was_save = False
    if form.is_valid():
       hasscreened = form.save(commit=False)
       hasscreened.scout_who_screened = request.user
       hasscreened.save()
       was_save = True
    return {"errors": form.errors, "was_save": was_save}


class CitizenCreateModalView(BSModalCreateView):
    template_name = "pages/backOffice/citizen_createModal.html"
    form_class = forms.CitizenModelForm
    success_message = "Citizen was successful create!"
    success_url = reverse_lazy('screening:has_screened-list')

    class Meta:
        model = Citizen


class CitizenCreateView(View):
    model = Citizen
    template_name = "pages/backOffice/citizens_list.html"
    form_class = forms.CitizenModelForm2

    def post(request):
        return JsonResponse({
            "Reçu": "daccord le serveur a bien reçu"
        })


class OnlineTestListView(generic.ListView):
    model = models.OnlineTest
    queryset = models.OnlineTest.objects.all()
    template_name = "pages/backOffice/online_tests_list.html"


class HasScreenedCreateModalView(BSModalCreateView):
    template_name = "pages/backOffice/has_screened_createModal.html"
    form_class = forms.HasScreenedModelForm
    success_message = "the citizen was successful screened"
    success_url = reverse_lazy('screening:has_screened-list')

    def form_valid(self, form):
        form.save(commit=False)
        form.scout_who_screened = self.request.user
        return HttpResponseRedirect(self.success_url)


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


class HasScreenedListView(generic.View):
    template_name = "pages/backOffice/citizens_list.html"

    def get(self, request):
        hasscreened_list = HasScreened.objects.filter(scout_who_screened=request.user)
        form = forms.CitizenModelForm2()

        form2 = forms.HasScreenedModelForm2()

        return render(request, template_name=self.template_name, context={
            'form': form,
            'form2': form2,
            'hl': hasscreened_list,
        })


# home_office
@login_required
def index_back_office(request):
    return render(request, 'pages/backOffice/index.html')


def forgot_password(request):
    return render(request, 'pages/backOffice/forgot-password.html')


def register(request):
    return render(request, 'pages/backOffice/register.html')


def map_index(request):
    return render(request, 'pages/backOffice/map.html')


from django.db.models import Sum


class OnlineTestView(View):
    comments = ['Rien d\'inquietant , restez juste confinez en suivant l\'evolution',
                'Cas urgent! , nous vous recontacterons', 'Vous nêtes pas sujet au covid']

    results = ['Potentiel positif', 'Potentiel negatif', 'Rien à signaler']

    def test_logic(self, symptoms_selected):
        total_values = models.Symptom.objects.aggregate(sum=Sum("value"))["sum"]
        curr_val = 0
        for sy in symptoms_selected:
            curr_val += sy.value
        return (curr_val / total_values) * 100

    def get(self, request):
        symptoms = models.Symptom.objects.all()
        form = forms.OnlineTestForm()
        form.set_data()
        return render(request, 'form_test/index.html', context={
            'symptoms': symptoms, 'form': form
        })

    def post(self, request):
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

                percentage = self.test_logic(symptoms)
                comment = str()
                result = str()
                if 0 <= percentage <= 25:
                    comment = self.comments[2]
                    result = self.results[2]
                elif 25 < percentage <= 50:
                    comment = self.comments[0]
                    result = self.results[1]
                else:
                    comment = self.comments[1]
                    result = self.results[0]

                citizen = Citizen.objects.create(first_name=first_name, second_name=second_name, birth_day=birth_day,
                                                 identity_card_id=id_card, gender=gender, mobile_phone=mobile_phone,
                                                 nationality=nationality)

                online_test = models.OnlineTest(citizen_who_has_been_tested=citizen, state=state, voice=voice,
                                                percentage=percentage, comments=comment, result=result)
                online_test.save()
                for sy in symptoms:
                    online_test.symptoms_detected.add(sy)

                return HttpResponse("Vous avez établit un score de {} , conclusion: {} ".format(percentage, comment))

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


# Vue du home
def index(request):
    hasScreenedByDate = HasScreened.objects.values('screening_date_date') \
        .annotate(
        positive=Count('status', filter=(Q(status='+') | Q(status='r') | Q(status='d'))),
        active=Count('status', filter=Q(status='+')),
        negative=Count('status', filter=Q(status='-')),
        indeterminate=Count('status', filter=Q(status='?')),
        recovered=Count('status', filter=Q(status='r')),
        dead=Count('status', filter=Q(status='d'))
    ) \
        .order_by('screening_date_date').distinct()
    hasScreenedByRegion = HasScreened.objects.values('region') \
        .annotate(
        positive=Count('status', filter=(Q(status='+') | Q(status='r') | Q(status='d'))),
        active=Count('status', filter=Q(status='+')),
        negative=Count('status', filter=Q(status='-')),
        indeterminate=Count('status', filter=Q(status='?')),
        recovered=Count('status', filter=Q(status='r')),
        dead=Count('status', filter=Q(status='d'))
    ) \
        .order_by('region')
    hasScreenedByAge = HasScreened.objects.values('citizen_who_has_been_screened__age') \
        .annotate(
        positive=Count('status', filter=(Q(status='+') | Q(status='r') | Q(status='d'))),
        active=Count('status', filter=Q(status='+')),
        negative=Count('status', filter=Q(status='-')),
        indeterminate=Count('status', filter=Q(status='?')),
        recovered=Count('status', filter=Q(status='r')),
        dead=Count('status', filter=Q(status='d'))
    ) \
        .order_by('citizen_who_has_been_screened__age')
    hasScreenedByGenre = HasScreened.objects.values('citizen_who_has_been_screened__gender') \
        .annotate(
        positive=Count('status', filter=(Q(status='+') | Q(status='r') | Q(status='d'))),
        active=Count('status', filter=Q(status='+')),
        negative=Count('status', filter=Q(status='-')),
        indeterminate=Count('status', filter=Q(status='?')),
        recovered=Count('status', filter=Q(status='r')),
        dead=Count('status', filter=Q(status='d'))
    ) \
        .order_by('citizen_who_has_been_screened__gender')
    hasScreenedByGenreAge = HasScreened.objects.values('citizen_who_has_been_screened__age') \
        .annotate(
        positiveM=Count('status', filter=((Q(status='+', citizen_who_has_been_screened__gender='M') | Q(status='r',
                                                                                                        citizen_who_has_been_screened__gender='M') | Q(
            status='d', citizen_who_has_been_screened__gender='M')))),
        activeM=Count('status', filter=Q(status='+', citizen_who_has_been_screened__gender='M')),
        negativeM=Count('status', filter=Q(status='-', citizen_who_has_been_screened__gender='M')),
        indeterminateM=Count('status', filter=Q(status='?', citizen_who_has_been_screened__gender='M')),
        recoveredM=Count('status', filter=Q(status='r', citizen_who_has_been_screened__gender='M')),
        deadM=Count('status', filter=Q(status='d', citizen_who_has_been_screened__gender='M')),
        positiveF=Count('status', filter=((Q(status='+', citizen_who_has_been_screened__gender='F') | Q(status='r',
                                                                                                        citizen_who_has_been_screened__gender='F') | Q(
            status='d', citizen_who_has_been_screened__gender='F')))),
        activeF=Count('status', filter=Q(status='+', citizen_who_has_been_screened__gender='F')),
        negativeF=Count('status', filter=Q(status='-', citizen_who_has_been_screened__gender='F')),
        indeterminateF=Count('status', filter=Q(status='?', citizen_who_has_been_screened__gender='F')),
        recoveredF=Count('status', filter=Q(status='r', citizen_who_has_been_screened__gender='F')),
        deadF=Count('status', filter=Q(status='d', citizen_who_has_been_screened__gender='F'))
    ) \
        .order_by('citizen_who_has_been_screened__age')

    map_cases_json = json.dumps(list(hasScreenedByRegion), cls=DjangoJSONEncoder)

    date = list()
    positiveDate = list()
    activeDate = list()
    recoveredDate = list()
    deadDate = list()

    region = list()
    positive = list()
    active = list()
    recovered = list()
    dead = list()

    age = list()
    activeAge = list()
    recoveredAge = list()
    deadAge = list()

    genre = list()
    activeGenre = list()
    recoveredGenre = list()
    deadGenre = list()

    ageAge = list()
    positiveM = list()
    activeM = list()
    recoveredM = list()
    deadM = list()
    positiveF = list()
    activeF = list()
    recoveredF = list()
    deadF = list()

    for entry0 in hasScreenedByDate:
        positiveDate.append(entry0['positive'])
        date.append(entry0['screening_date_date'])
        activeDate.append(entry0['active'])
        recoveredDate.append(entry0['recovered'])
        deadDate.append(entry0['dead'])

    for entry1 in hasScreenedByRegion:
        positive.append(entry1['positive'])
        region.append(entry1['region'])
        active.append(entry1['active'])
        recovered.append(entry1['recovered'])
        dead.append(entry1['dead'])

    for entry2 in hasScreenedByAge:
        age.append('%s ans' % entry2['citizen_who_has_been_screened__age'])
        activeAge.append(entry2['active'])
        recoveredAge.append(entry2['recovered'])
        deadAge.append(entry2['dead'])

    for entry3 in hasScreenedByGenre:
        genre.append(entry3['citizen_who_has_been_screened__gender'])
        activeGenre.append(entry3['active'])
        recoveredGenre.append(entry3['recovered'])
        deadGenre.append(entry3['dead'])

    for i in range(21):
        if i >= 20:
            ageAge.append('100 +')
        else:
            ageAge.append('%d-%d' % (i * 5, (i * 5) + 4))
        activeM.append(0)
        activeF.append(0)
        recoveredM.append(0)
        deadM.append(0)
        deadF.append(-0)
        activeF.append(-0)
        recoveredF.append(-0)
        positiveF.append(-0)
        positiveM.append(0)
    for entry4 in hasScreenedByGenreAge:
        if 0 <= entry4['citizen_who_has_been_screened__age'] < 5:
            activeM[0] = entry4['activeM']
            recoveredM[0] = entry4['recoveredM']
            deadM[0] = entry4['deadM']
            activeF[0] = -entry4['activeF']
            recoveredF[0] = -entry4['recoveredF']
            deadF[0] = -entry4['deadF']
            positiveF[0] = -entry4['positiveF']
            positiveM[0] = entry4['positiveM']
        if 5 <= entry4['citizen_who_has_been_screened__age'] < 10:
            activeM[1] = entry4['activeM']
            recoveredM[1] = entry4['recoveredM']
            deadM[1] = entry4['deadM']
            activeF[1] = -entry4['activeF']
            recoveredF[1] = -entry4['recoveredF']
            deadF[1] = -entry4['deadF']
            positiveF[1] = -entry4['positiveF']
            positiveM[1] = entry4['positiveM']
        if 10 <= entry4['citizen_who_has_been_screened__age'] < 15:
            activeM[2] = entry4['activeM']
            recoveredM[2] = entry4['recoveredM']
            deadM[2] = entry4['deadM']
            activeF[2] = -entry4['activeF']
            recoveredF[2] = -entry4['recoveredF']
            deadF[2] = -entry4['deadF']
            positiveF[2] = -entry4['positiveF']
            positiveM[2] = entry4['positiveM']
        if 15 <= entry4['citizen_who_has_been_screened__age'] < 20:
            activeM[3] = entry4['activeM']
            recoveredM[3] = entry4['recoveredM']
            deadM[3] = entry4['deadM']
            activeF[3] = -entry4['activeF']
            recoveredF[3] = -entry4['recoveredF']
            deadF[3] = -entry4['deadF']
            positiveF[3] = -entry4['positiveF']
            positiveM[3] = entry4['positiveM']
        if 20 <= entry4['citizen_who_has_been_screened__age'] < 25:
            activeM[4] = entry4['activeM']
            recoveredM[4] = entry4['recoveredM']
            deadM[4] = entry4['deadM']
            activeF[4] = -entry4['activeF']
            recoveredF[4] = -entry4['recoveredF']
            deadF[4] = -entry4['deadF']
            positiveF[4] = -entry4['positiveF']
            positiveM[4] = entry4['positiveM']
        if 25 <= entry4['citizen_who_has_been_screened__age'] < 30:
            activeM[5] = entry4['activeM']
            recoveredM[5] = entry4['recoveredM']
            deadM[5] = entry4['deadM']
            activeF[5] = -entry4['activeF']
            recoveredF[5] = -entry4['recoveredF']
            deadF[5] = -entry4['deadF']
            positiveF[5] = -entry4['positiveF']
            positiveM[5] = entry4['positiveM']
        if 30 <= entry4['citizen_who_has_been_screened__age'] < 35:
            activeM[6] = entry4['activeM']
            recoveredM[6] = entry4['recoveredM']
            deadM[6] = entry4['deadM']
            activeF[6] = -entry4['activeF']
            recoveredF[6] = -entry4['recoveredF']
            deadF[6] = -entry4['deadF']
            positiveF[6] = -entry4['positiveF']
            positiveM[6] = entry4['positiveM']
        if 35 <= entry4['citizen_who_has_been_screened__age'] < 40:
            activeM[7] = entry4['activeM']
            recoveredM[7] = entry4['recoveredM']
            deadM[7] = entry4['deadM']
            activeF[7] = -entry4['activeF']
            recoveredF[7] = -entry4['recoveredF']
            deadF[7] = -entry4['deadF']
            positiveF[7] = -entry4['positiveF']
            positiveM[7] = entry4['positiveM']
        if 40 <= entry4['citizen_who_has_been_screened__age'] < 45:
            activeM[8] = entry4['activeM']
            recoveredM[8] = entry4['recoveredM']
            deadM[8] = entry4['deadM']
            activeF[8] = -entry4['activeF']
            recoveredF[8] = -entry4['recoveredF']
            deadF[8] = -entry4['deadF']
            positiveF[8] = -entry4['positiveF']
            positiveM[8] = entry4['positiveM']
        if 45 <= entry4['citizen_who_has_been_screened__age'] < 50:
            activeM[9] = entry4['activeM']
            recoveredM[9] = entry4['recoveredM']
            deadM[9] = entry4['deadM']
            activeF[9] = -entry4['activeF']
            recoveredF[9] = -entry4['recoveredF']
            deadF[9] = -entry4['deadF']
            positiveF[9] = -entry4['positiveF']
            positiveM[9] = entry4['positiveM']
        if 50 <= entry4['citizen_who_has_been_screened__age'] < 55:
            activeM[10] = entry4['activeM']
            recoveredM[10] = entry4['recoveredM']
            deadM[10] = entry4['deadM']
            activeF[10] = -entry4['activeF']
            recoveredF[10] = -entry4['recoveredF']
            deadF[10] = -entry4['deadF']
            positiveF[10] = -entry4['positiveF']
            positiveM[10] = entry4['positiveM']
        if 55 <= entry4['citizen_who_has_been_screened__age'] < 60:
            activeM[11] = entry4['activeM']
            recoveredM[11] = entry4['recoveredM']
            deadM[11] = entry4['deadM']
            activeF[11] = -entry4['activeF']
            recoveredF[11] = -entry4['recoveredF']
            deadF[11] = -entry4['deadF']
            positiveF[11] = -entry4['positiveF']
            positiveM[11] = entry4['positiveM']
        if 60 <= entry4['citizen_who_has_been_screened__age'] < 65:
            activeM[12] = entry4['activeM']
            recoveredM[12] = entry4['recoveredM']
            deadM[12] = entry4['deadM']
            activeF[12] = -entry4['activeF']
            recoveredF[12] = -entry4['recoveredF']
            deadF[12] = -entry4['deadF']
            positiveF[12] = -entry4['positiveF']
            positiveM[12] = entry4['positiveM']
        if 65 <= entry4['citizen_who_has_been_screened__age'] < 70:
            activeM[13] = entry4['activeM']
            recoveredM[13] = entry4['recoveredM']
            deadM[13] = entry4['deadM']
            activeF[13] = -entry4['activeF']
            recoveredF[13] = -entry4['recoveredF']
            deadF[13] = -entry4['deadF']
            positiveF[13] = -entry4['positiveF']
            positiveM[13] = entry4['positiveM']
        if 70 <= entry4['citizen_who_has_been_screened__age'] < 75:
            activeM[14] = entry4['activeM']
            recoveredM[14] = entry4['recoveredM']
            deadM[14] = entry4['deadM']
            activeF[14] = -entry4['activeF']
            recoveredF[14] = -entry4['recoveredF']
            deadF[14] = -entry4['deadF']
            positiveF[14] = -entry4['positiveF']
            positiveM[14] = entry4['positiveM']
        if 75 <= entry4['citizen_who_has_been_screened__age'] < 80:
            activeM[15] = entry4['activeM']
            recoveredM[15] = entry4['recoveredM']
            deadM[15] = entry4['deadM']
            activeF[15] = -entry4['activeF']
            recoveredF[15] = -entry4['recoveredF']
            deadF[15] = -entry4['deadF']
            positiveF[15] = -entry4['positiveF']
            positiveM[15] = entry4['positiveM']
        if 80 <= entry4['citizen_who_has_been_screened__age'] < 85:
            activeM[16] = entry4['activeM']
            recoveredM[16] = entry4['recoveredM']
            deadM[16] = entry4['deadM']
            activeF[16] = -entry4['activeF']
            recoveredF[16] = -entry4['recoveredF']
            deadF[16] = -entry4['deadF']
            positiveF[16] = -entry4['positiveF']
            positiveM[16] = entry4['positiveM']
        if 85 <= entry4['citizen_who_has_been_screened__age'] < 90:
            activeM[17] = entry4['activeM']
            recoveredM[17] = entry4['recoveredM']
            deadM[17] = entry4['deadM']
            activeF[17] = -entry4['activeF']
            recoveredF[17] = -entry4['recoveredF']
            deadF[17] = -entry4['deadF']
            positiveF[17] = -entry4['positiveF']
            positiveM[17] = entry4['positiveM']
        if 90 <= entry4['citizen_who_has_been_screened__age'] < 95:
            activeM[18] = entry4['activeM']
            recoveredM[18] = entry4['recoveredM']
            deadM[18] = entry4['deadM']
            activeF[18] = -entry4['activeF']
            recoveredF[18] = -entry4['recoveredF']
            deadF[18] = -entry4['deadF']
            positiveF[18] = -entry4['positiveF']
            positiveM[18] = entry4['positiveM']
        if 95 <= entry4['citizen_who_has_been_screened__age'] < 100:
            activeM[19] = entry4['activeM']
            recoveredM[19] = entry4['recoveredM']
            deadM[19] = entry4['deadM']
            activeF[19] = -entry4['activeF']
            recoveredF[19] = -entry4['recoveredF']
            deadF[19] = -entry4['deadF']
            positiveF[19] = -entry4['positiveF']
            positiveM[19] = entry4['positiveM']
        if entry4['citizen_who_has_been_screened__age'] >= 100:
            activeM[20] = entry4['activeM']
            recoveredM[20] = entry4['recoveredM']
            deadM[20] = entry4['deadM']
            activeF[20] = -entry4['activeF']
            recoveredF[20] = -entry4['recoveredF']
            deadF[20] = -entry4['deadF']
            positiveF[20] = -entry4['positiveF']
            positiveM[20] = entry4['positiveM']

    ageAge.reverse()
    activeM.reverse()
    recoveredM.reverse()
    deadM.reverse()
    activeF.reverse()
    recoveredF.reverse()
    deadF.reverse()
    positiveF.reverse()
    positiveM.reverse()

    positivesDate = {
        'name': 'Positifs',
        'data': positiveDate,
        'color': 'black'
    }
    activesDate = {
        'name': 'Actifs',
        'data': activeDate,
        'color': 'orange'
    }
    recoveredsDate = {
        'name': 'Gueris',
        'data': recoveredDate,
        'color': 'green'
    }
    deadsDate = {
        'name': 'Deces',
        'data': deadDate,
        'color': 'red'
    }

    actives = {
        'type': 'column',
        'name': 'Actifs',
        'data': active,
        'color': 'orange'
    }
    recovereds = {
        'type': 'column',
        'name': 'Gueris',
        'data': recovered,
        'color': 'green'
    }
    deads = {
        'type': 'column',
        'name': 'Deces',
        'data': dead,
        'color': 'red'
    }

    activesAge = {
        'type': 'spline',
        'name': 'Actifs',
        'data': activeAge,
        'color': 'orange'
    }
    recoveredsAge = {
        'type': 'spline',
        'name': 'Gueris',
        'data': recoveredAge,
        'color': 'green'
    }
    deadsAge = {
        'type': 'spline',
        'name': 'Deces',
        'data': deadAge,
        'color': 'red'
    }

    activesGenre = {
        'type': 'bar',
        'name': 'Actifs',
        'data': activeGenre,
        'color': 'orange'
    }
    recoveredsGenre = {
        'type': 'bar',
        'name': 'Gueris',
        'data': recoveredGenre,
        'color': 'green'
    }
    deadsGenre = {
        'type': 'bar',
        'name': 'Deces',
        'data': deadGenre,
        'color': 'red'
    }

    positivesM = {
        # 'type': 'bar',
        'name': 'Masculin',
        'data': positiveM,
        'color': '#000000'
    }
    positivesF = {
        # 'type': 'bar',
        'name': 'Feminin',
        'data': positiveF,
        'color': '#a8a8a8'
    }
    activesM = {
        # 'type': 'bar',
        'name': 'Masculin',
        'data': activeM,
        'color': '#ef6c00'
    }
    activesF = {
        # 'type': 'bar',
        'name': 'Feminin',
        'data': activeF,
        'color': '#ffe0b2'
    }
    recoveredsM = {
        # 'type': 'bar',
        'name': 'Masculin',
        'data': recoveredM,
        'color': '#2e7d32'
    }
    recoveredsF = {
        # 'type': 'bar',
        'name': 'Feminin',
        'data': recoveredF,
        'color': '#c8e6c9'
    }
    deadsM = {
        # 'type': 'bar',
        'name': 'Masculin',
        'data': deadM,
        'color': '#c62828'
    }
    deadsF = {
        # 'type': 'bar',
        'name': 'Feminin',
        'data': deadF,
        'color': '#ffcdd2'
    }

    chart_main_statistics = {
        'chart': {
            'type': 'spline',
            # 'backgroundColor': {
            #     'linearGradient': [0, 0, 0, 400],
            #     'stops': [
            #     [0, '#D9D9D9'],
            #     [1, '#D9D9D9']
            #     ]
            # },
            # 'plotBackgroundColor': "#fff",
            'borderWidth': 0,
            'borderRadius': 5,
            'plotShadow': "false",
            'plotBorderWidth': 0
        },
        'title': {'text': 'Nouveaux cas par jours'},
        'xAxis': {
            'categories': date,
            'title': {
                'text': 'Dates',
            },
        },
        'yAxis': {
            'min': 0,
            'allowDecimals': "false",
            'title': {
                'text': 'Cas',
            },
            'labels': {
                'overflow': 'justify'
            }
        },
        'series': [positivesDate, activesDate, recoveredsDate, deadsDate],
    }

    chart_cases = {
        'chart': {
            # 'type': 'column',
            # 'backgroundColor': {
            #     'linearGradient': [0, 0, 0, 400],
            #     'stops': [
            #     [0, '#D9D9D9'],
            #     [1, '#D9D9D9']
            #     ]
            # },
            # 'plotBackgroundColor': "#fff",
            'borderWidth': 0,
            'borderRadius': 5,
            'plotShadow': "false",
            'plotBorderWidth': 0
        },
        'title': {'text': 'Par regions'},
        'xAxis': {
            'categories': region,
            'title': {
                'text': 'Regions',
            },
        },
        'yAxis': {
            'min': 0,
            'allowDecimals': "false",
            'title': {
                'text': 'Cas',
            },
            'labels': {
                'overflow': 'justify'
            }
        },
        'series': [actives, recovereds, deads],
        # 'legend': {
        #   'layout': 'vertical',
        #   'align': 'right',
        #   'verticalAlign': 'top',
        #   'x': -40,
        #   'y': 80,
        #   'floating': "true",
        #   'borderWidth': 1,
        #   'backgroundColor': '#FFFFFF',
        #   'shadow': "true"
        # },
        # 'tooltip': {
        #     'backgroundColor': {
        #         'linearGradient': [0, 0, 0, 60],
        #         'stops': [
        #             [0, '#FFFFFF'],
        #             [1, '#E0E0E0']
        #         ]
        #     },
        #     'borderWidth': 1,
        #     'borderColor': '#AAA',
        #     'borderRadius': 2
        # },
    }

    chart_ages_cases = {
        'chart': {
            # 'type': 'pie',
            # 'polar': 'true',
            # 'backgroundColor': {
            #     'linearGradient': [0, 0, 0, 400],
            #     'stops': [
            #     [0, '#D9D9D9'],
            #     [1, '#D9D9D9']
            #     ]
            # },
            # 'plotBackgroundColor': "#fff",
            'borderWidth': 0,
            'borderRadius': 5,
            'plotShadow': "false",
            'plotBorderWidth': 0
        },
        'title': {'text': 'Par age'},
        'xAxis': {
            'categories': age,
            'title': {
                'text': 'Age',
            },
        },
        'yAxis': {
            'min': 0,
            'title': {
                'text': 'Cas',
            },
            'labels': {
                'overflow': 'justify'
            }
        },
        'series': [activesAge, recoveredsAge, deadsAge],
    }

    chart_genre_cases = {
        'chart': {
            # 'type': 'pie',
            # 'polar': 'true',
            # 'backgroundColor': {
            #     'linearGradient': [0, 0, 0, 400],
            #     'stops': [
            #     [0, '#D9D9D9'],
            #     [1, '#D9D9D9']
            #     ]
            # },
            # 'plotBackgroundColor': "#fff",
            'borderWidth': 0,
            'borderRadius': 5,
            'plotShadow': "false",
            'plotBorderWidth': 0
        },
        'title': {'text': 'Par genre'},
        'xAxis': {
            'categories': genre,
            'title': {
                'text': 'Genre',
            },
        },
        'yAxis': {
            'min': 0,
            'title': {
                'text': 'Cas',
                # 'align': 'high'
            },
            'labels': {
                'overflow': 'justify'
            }
        },
        'series': [activesGenre, recoveredsGenre, deadsGenre],
    }

    chart_genre_age_cases_positive = {
        'chart': {
            'type': 'bar',
            # 'polar': 'true',
            # 'backgroundColor': {
            #     'linearGradient': [0, 0, 0, 400],
            #     'stops': [
            #     [0, '#D9D9D9'],
            #     [1, '#D9D9D9']
            #     ]
            # },
            # 'plotBackgroundColor': "#fff",
            'borderWidth': 0,
            'borderRadius': 5,
            'plotShadow': "false",
            'plotBorderWidth': 0
        },
        'title': {'text': 'Par genre : positifs'},
        'xAxis': [
            {
                'categories': ageAge,
                'reversed': 'false',
                'labels': {
                    'step': 1
                },
                'accessibility': {
                    'description': 'Age (masculin)'
                }
            },
            {
                'opposite': 'false',
                'reversed': 'false',
                'categories': ageAge,
                'linkedTo': 0,
                'labels': {
                    'step': 1
                },
                'accessibility': {
                    'description': 'Age (feminin)'
                }
            }
        ],
        'yAxis': {
            'title': {
                'text': 'Cas actifs',
            },
            'accessibility': {
                'description': 'Positifs du covid-19 par genre',
                'rangeDescription': ''
            }
        },
        'plotOptions': {
            'series': {
                'stacking': 'normal'
            }
        },
        'series': [
            positivesF,
            positivesM
        ]
    }

    chart_genre_age_cases_recovered = {
        'chart': {
            'type': 'bar',
            # 'polar': 'true',
            # 'backgroundColor': {
            #     'linearGradient': [0, 0, 0, 400],
            #     'stops': [
            #     [0, '#D9D9D9'],
            #     [1, '#D9D9D9']
            #     ]
            # },
            # 'plotBackgroundColor': "#fff",
            'borderWidth': 0,
            'borderRadius': 5,
            'plotShadow': "false",
            'plotBorderWidth': 0
        },
        'title': {'text': 'Par genre : gueris'},
        'xAxis': [
            {
                'categories': ageAge,
                'reversed': 'false',
                'labels': {
                    'step': 1
                },
                'accessibility': {
                    'description': 'Age (masculin)'
                }
            },
            {
                'opposite': 'false',
                'reversed': 'false',
                'categories': ageAge,
                'linkedTo': 0,
                'labels': {
                    'step': 1
                },
                'accessibility': {
                    'description': 'Age (feminin)'
                }
            }
        ],
        'yAxis': {
            'title': {
                'text': 'Gueris',
            },
            'accessibility': {
                'description': 'Cas de guerison du covid-19 par genre',
                'rangeDescription': ''
            }
        },
        'plotOptions': {
            'series': {
                'stacking': 'normal'
            }
        },
        'series': [
            recoveredsF,
            recoveredsM
        ]
    }

    chart_genre_age_cases_dead = {
        'chart': {
            'type': 'bar',
            'polar': 'true',
            # 'backgroundColor': {
            #     'linearGradient': [0, 0, 0, 400],
            #     'stops': [
            #         [0, '#D9D9D9'],
            #         [1, '#D9D9D9']
            #     ]
            # },
            # 'plotBackgroundColor': "#fff",
            'borderWidth': 0,
            'borderRadius': 5,
            'plotShadow': "false",
            'plotBorderWidth': 0
        },
        'title': {'text': 'Par genre : decedes'},
        'xAxis': [
            {
                'categories': ageAge,
                'reversed': 'false',
                'labels': {
                    'step': 1
                },
                'accessibility': {
                    'description': 'Age (masculin)'
                }
            },
            {
                'opposite': 'false',
                'reversed': 'false',
                'categories': ageAge,
                'linkedTo': 0,
                'labels': {
                    'step': 1
                },
                'accessibility': {
                    'description': 'Age (feminin)'
                }
            }
        ],
        'yAxis':
            {
                'title': {
                    'text': 'Deces',
                },
                'accessibility': {
                    'description': 'Evolution des deces du covid-19 par genre',
                    'rangeDescription': ''
                }
            },
        'plotOptions': {
            'series': {
                'stacking': 'normal'
            }
        },
        'series': [
            deadsF,
            deadsM
        ]
    }

    chart_cases = json.dumps(chart_cases)
    chart_ages_cases = json.dumps(chart_ages_cases)
    chart_genre_cases = json.dumps(chart_genre_cases)
    chart_genre_age_cases_positive = json.dumps(chart_genre_age_cases_positive)
    chart_genre_age_cases_recovered = json.dumps(chart_genre_age_cases_recovered)
    chart_genre_age_cases_dead = json.dumps(chart_genre_age_cases_dead)
    chart_main_statistics = json.dumps(chart_main_statistics, indent=4, sort_keys=True, default=str)
    return render(request, 'pages/index/index.html',
                  {'chart_cases': chart_cases, 'map_cases_json': map_cases_json, 'chart_ages_cases': chart_ages_cases,
                   'chart_genre_cases': chart_genre_cases,
                   'chart_genre_age_cases_positive': chart_genre_age_cases_positive,
                   'chart_genre_age_cases_recovered': chart_genre_age_cases_recovered,
                   'chart_genre_age_cases_dead': chart_genre_age_cases_dead,
                   'chart_main_statistics': chart_main_statistics})
