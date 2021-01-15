from django.forms import ModelForm, Form
from django import forms
from . import models
from django.views.generic import CreateView
from bootstrap_modal_forms.forms import BSModalModelForm, BSModalForm


class CitizenModelForm(BSModalModelForm):
    class Meta:
        model = models.Citizen
        exclude = ['age']


class CitizenModelForm2(forms.ModelForm):
    class Meta:
        model = models.Citizen
        exclude = ['age']

    def to_form_small(self):
        for key in self.fields:
            self.fields[key].widget.attrs.update({'class': 'form-control-sm', 'style': '{margin-bottom: 18px;}'})


class HasScreenedModelForm2(forms.ModelForm):
    class Meta:
        model = models.HasScreened
        exclude = ["scout_who_screened"]


class HasScreenedModelForm(BSModalModelForm):
    class Meta:
        model = models.HasScreened
        exclude = ['scout_who_screened']


class OnlineTestForm(Form):
    first_name = forms.CharField(max_length=30, help_text="Enter your first name here")
    second_name = forms.CharField(max_length=30, required=False)
    birth_day = forms.DateField(help_text="the format of date is yyyy-mm-dd")
    gender = forms.ChoiceField(choices=[('M', 'M'), ('F', 'F')], widget=forms.Select)
    identity_card_id = forms.CharField(max_length=255, required=False)
    nationality = forms.ChoiceField(choices=[
        ('Cmr', 'Cameroonian'),
        ('Oth', 'Others')
    ])
    mobile_phone = forms.CharField(max_length=20, required=True, initial="+237")
    state = forms.CharField(widget=forms.Textarea(attrs={'cols': 'auto', 'rows': 'auto'}), required=False)
    voice = forms.FileField(widget=forms.FileInput, required=False)

    def set_data(self):
        self.fields['symptoms'] = forms.ModelMultipleChoiceField(queryset=models.Symptom.objects.all(),
                                                                 widget=forms.CheckboxSelectMultiple())
