# -*- coding: utf-8 -*-
# Generated by Django 1.10.6 on 2017-04-27 16:22
from __future__ import unicode_literals

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('flylo', '0004_auto_20170427_1617'),
    ]

    operations = [
        migrations.AddField(
            model_name='flight',
            name='airline',
            field=models.ForeignKey(default=0, on_delete=django.db.models.deletion.CASCADE, to='flylo.Airline'),
            preserve_default=False,
        ),
        migrations.AddField(
            model_name='flight',
            name='airplane',
            field=models.ForeignKey(default=0, on_delete=django.db.models.deletion.CASCADE, to='flylo.Airplane'),
            preserve_default=False,
        ),
    ]
