# -*- coding: utf-8 -*-
# Generated by Django 1.10.6 on 2017-05-11 20:27
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('flylo', '0001_initial'),
    ]

    operations = [
        migrations.AddField(
            model_name='flight',
            name='price',
            field=models.DecimalField(decimal_places=2, default=0, max_digits=12),
        ),
    ]
