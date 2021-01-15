from django.urls import path
from . import views

app_name = "screening"

urlpatterns = [

    # Acceuil screening
    path('login', views.LoginView.as_view(), name="login"),
    path('', views.index, name="home"),
    path('backOffice', views.index_back_office, name="home_back_office"),
    path('forgot-password', views.forgot_password, name='forgot_password'),
    path('register', views.register, name='register'),
    path('online_test', views.OnlineTestView.as_view(), name="online_test"),


    path('citizens/', views.HasScreenedListView.as_view(), name='has_screened-list'),
    # path('citizens/<int:pk>', views.CitizenDetailView.as_view(), name='citizens-detail'),
    path(r'citizens/create/$', views.CitizenCreateModalView.as_view(), name="citizens-create"),

    path('citizens/create_2', views.CitizenCreateView.as_view(), name="citizens-create_2"),

    path('has_screened/create', views.HasScreenedCreateModalView.as_view(), name="has_screened-create"),
    path('has_screened/<int:pk>', views.HasScreenedReadModalView.as_view(), name="has_screened-detail"),
    path('has_screened/update/<int:pk>', views.HasScreenedUpdateModalView.as_view(), name="has_screened-update"),
    path('has_screened/delete/<int:pk>', views.HasScreenedDeleteModalView.as_view(), name="has_screened-delete"),

    path('map', views.map_index, name="map"),
    path('online_test/', views.OnlineTestListView.as_view(), name="online_test-list"),

    path('messages', views.PersonalMessageView.as_view(), name="message-personal"),

    path('citizen_ajax_save', views.save_citizen, name="citizen-save-ajax"),
    path('screening_ajax_save', views.save_screening, name="screening-save-ajax")

]
