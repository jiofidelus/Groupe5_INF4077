from rest_framework import viewsets
from . import models
from . import serializers


# Create your views here.
class DoctorViewSet(viewsets.ViewSet):
    serializer_class = serializers.DoctorSerializer
    query_set = models.Doctor.objects.all()


class ScoutViewSet(viewsets.ViewSet):
    queryset = models.Scout.objects.all()
    serializer_class = serializers.ScoutSerializer


class SymptomViewSet(viewsets.ViewSet):
    queryset = models.Symptom.objects.all()
    serializer_class = serializers.SymptomSerializer


class OnlineTestViewSet(viewsets.ViewSet):
    queryset = models.OnlineTest.objects.all()
    serializer_class = serializers.OnlineTestSerializer


class CitizenViewSet(viewsets.ViewSet):
    queryset = models.Citizen.objects.all()
    serializer_class = serializers.CitizenSerializer
