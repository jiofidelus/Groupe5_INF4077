from rest_framework import viewsets
from . import models
from . import serializers


# Create your views here.
class UserViewSet(viewsets.ModelViewSet):
    queryset = models.User.objects.all()
    serializer_class = serializers.UserSerializer


class DoctorViewSet(viewsets.ModelViewSet):
    queryset = models.Doctor.objects.all()
    serializer_class = serializers.DoctorSerializer


class ScoutViewSet(viewsets.ModelViewSet):
    queryset = models.Scout.objects.all()
    serializer_class = serializers.ScoutSerializer


class SymptomViewSet(viewsets.ModelViewSet):
    queryset = models.Symptom.objects.all()
    serializer_class = serializers.SymptomSerializer


class OnlineTestViewSet(viewsets.ModelViewSet):
    queryset = models.OnlineTest.objects.all()
    serializer_class = serializers.OnlineTestSerializer


class CitizenViewSet(viewsets.ModelViewSet):
    queryset = models.Citizen.objects.all()
    serializer_class = serializers.CitizenSerializer
