# -*- coding: utf-8 -*-
# Generated by Django 1.9.4 on 2017-05-04 16:04
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('flylo', '0008_auto_20170504_1602'),
    ]

    operations = [
        migrations.AlterField(
            model_name='reservation',
            name='seat',
            field=models.CharField(max_length=4, null=True),
        ),
    ]
