from rest_framework import serializers
from . import models


class CitizenSerializer(serializers.HyperlinkedModelSerializer):
    class Meta:
        model = models.Citizen
        fields = "__all__"


class DoctorSerializer(serializers.HyperlinkedModelSerializer):
    class Meta:
        model = models.Doctor
        fields = "__all__"


class ScoutSerializer(serializers.HyperlinkedModelSerializer):
    class Meta:
        model = models.Scout
        fields = "__all__"


class SymptomSerializer(serializers.HyperlinkedModelSerializer):
    class Meta:
        model = models.Symptom
        fields = "__all__"


class OnlineTestSerializer(serializers.HyperlinkedModelSerializer):
    class Meta:
        model = models.OnlineTest
        fields = "__all__"

