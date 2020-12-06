# Generated by Django 3.1.1 on 2020-12-05 16:41

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('covid_api_rest', '0007_auto_20201205_1634'),
    ]

    operations = [
        migrations.AlterField(
            model_name='citizen',
            name='mobile_phone',
            field=models.IntegerField(),
        ),
        migrations.AlterField(
            model_name='doctor',
            name='mobile_phone_working',
            field=models.IntegerField(),
        ),
        migrations.AlterField(
            model_name='hasscreened',
            name='region',
            field=models.CharField(choices=[('Adamaoua', 'Adamaoua'), ('East', 'East'), ('West', 'West'), ('North', 'North'), ('South', 'South'), ('Littoral', 'Littoral'), ('Centre', 'Centre'), ('Extreme_North', 'Extreme_North'), ('South_West', 'South_West'), ('North_West', 'North_West')], max_length=13),
        ),
        migrations.AlterField(
            model_name='scout',
            name='mobile_phone_working',
            field=models.IntegerField(),
        ),
    ]
