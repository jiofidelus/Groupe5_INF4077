from . import views
from rest_framework import routers
from django.urls import include, path

router = routers.DefaultRouter()
router.register(r'citizens', views.CitizenViewSet, basename='citizen')
router.register(r'doctors', views.DoctorViewSet, basename='doctor')
router.register(r'symptoms', views.SymptomViewSet, basename='symptom')
router.register(r'scouts', views.ScoutViewSet, basename='scout')
router.register(r'online_tests', views.OnlineTestViewSet, basename='online_test')

urlpatterns = [
    path('', include(router.urls)),
    path('api-auth/', include('rest_framework.urls', namespace='rest_framework'))
]
