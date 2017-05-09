# -*- coding: utf-8 -*-
# Generated by Django 1.9.4 on 2017-05-04 13:41
from __future__ import unicode_literals

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('flylo', '0006_auto_20170502_1320'),
    ]

    operations = [
        migrations.CreateModel(
            name='Seat',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('code', models.CharField(max_length=4)),
                ('type', models.CharField(max_length=2)),
                ('price', models.FloatField()),
                ('confirmed', models.BooleanField(default=False)),
                ('added', models.DateTimeField(auto_now_add=True)),
                ('flight', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='flylo.Flight')),
            ],
        ),
    ]