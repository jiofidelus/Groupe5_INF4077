from . import views
from rest_framework import routers
from django.urls import include, path

router = routers.DefaultRouter()
router.register(r'citizens', views.CitizenViewSet, basename='citizens')
router.register(r'doctors', views.DoctorViewSet, basename='doctors')
router.register(r'symptoms', views.SymptomViewSet, basename='symptoms')
router.register(r'scouts', views.ScoutViewSet, basename='scouts')
router.register(r'online_tests', views.OnlineTestViewSet, basename='online_tests')

urlpatterns = [
    path('', include(router.urls)),
    path('api-auth/', include('rest_framework.urls', namespace='rest_framework'))
]
