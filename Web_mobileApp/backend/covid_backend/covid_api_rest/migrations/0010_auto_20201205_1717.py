# Generated by Django 3.1.1 on 2020-12-05 17:17

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('covid_api_rest', '0009_auto_20201205_1659'),
    ]

    operations = [
        migrations.AddField(
            model_name='citizen',
            name='register_date',
            field=models.DateTimeField(auto_now=True),
        ),
        migrations.AlterField(
            model_name='hasscreened',
            name='region',
            field=models.CharField(choices=[('North_West', 'North_West'), ('East', 'East'), ('South_West', 'South_West'), ('West', 'West'), ('South', 'South'), ('Adamaoua', 'Adamaoua'), ('Extreme_North', 'Extreme_North'), ('North', 'North'), ('Littoral', 'Littoral'), ('Centre', 'Centre')], max_length=13),
        ),
    ]
