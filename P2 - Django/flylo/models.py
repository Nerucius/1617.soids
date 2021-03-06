from __future__ import unicode_literals
from django.db import models

from django.contrib.auth.models import User
from django.db.models.signals import post_save
from django.dispatch import receiver

from decimal import Decimal

# Choices for Reservation type
FLIGHT_CLASS = (
	('e', 'Economy'),
	('b', 'Business'),
	('f', 'First Class'),
)

TYPE_MULT = {
	'e': 1,
	'b': 1.5,
	'f': 2.5
}


class Client(models.Model):
	""" Client model attached to an Auth User model. """
	user = models.OneToOneField(User, on_delete=models.CASCADE)
	money = models.DecimalField(max_digits=12, decimal_places=2, default=0)

	@property
	def reservations(self):
		return Reservation.objects.filter(client=self)

	@property
	def total_spent(self):
		total = Decimal('0.00')
		paid_res = Reservation.objects.filter(client=self)
		for res in paid_res:
			total += res.price
		return total

	def __unicode__(self):
		return self.user.username


@receiver(post_save, sender=User)
def create_client(sender, instance, created, **kwargs):
	if created:
		Client.objects.create(user=instance)


@receiver(post_save, sender=User)
def save_user_profile(sender, instance, **kwargs):
	instance.client.save()


class Airline(models.Model):
	code = models.CharField(max_length=3)
	price = models.DecimalField(decimal_places=2, max_digits=12, default=0)

	def __str__(self):
		return self.code


class Airplane(models.Model):
	aircraft = models.CharField(max_length=4)
	seats_first_class = models.IntegerField()
	seats_business = models.IntegerField()
	seats_economy = models.IntegerField()

	def __str__(self):
		return self.aircraft


class Flight(models.Model):
	# foreign keys
	airlines = models.ManyToManyField(Airline)
	airplane = models.ForeignKey(Airplane, on_delete=models.CASCADE)

	# fields
	flight_number = models.CharField(max_length=8)
	estimated_time_departure = models.DateTimeField()
	estimated_time_arrival = models.DateTimeField()
	location_departure = models.CharField(max_length=3)
	location_arrival = models.CharField(max_length=3)
	status = models.CharField(max_length=40)
	base_price = models.DecimalField(decimal_places=2, max_digits=12, default=0)

	@property
	def computed_price(self):
		""" Price of the flight. The further in time the flight is, the cheaper it is.
			Conversely, the fuller the plane is, the more expensive the flight. """
		from datetime import datetime
		import pytz

		ap = self.airplane
		total_places = ap.seats_first_class + ap.seats_economy + ap.seats_business
		res = Reservation.objects.filter(flight=self, paid=True).count()
		fillfactor = float(res) / float(total_places)
		fillmulti = 1 + fillfactor * 1.5

		now = datetime.now(tz=pytz.utc)
		departure = self.estimated_time_departure
		days = min(120, max(0, (departure - now).days))

		return self.base_price * Decimal((1 - (days * 0.003)) * fillmulti)

	@property
	def price(self):
		""" Price of the flight. The further in time the flight is, the cheaper it is.
			Conversely, the fuller the plane is, the more expensive the flight. """

		cheap = self.airlines.all()[0].price
		for airline in self.airlines.all():
			cheap = min(cheap, airline.price)

		return round(self.computed_price + cheap, 2)

	class Meta:
		# ordering by logical departure order
		ordering = ['estimated_time_departure']

	# ordering by logical arrival order
	# ordering = ['estimated_time_arrival', 'location_departure']

	def __str__(self):
		return "[" + self.flight_number + "] " + self.location_departure + "-" + self.location_arrival + " : " + str(
			self.estimated_time_departure) + " / " + str(self.estimated_time_arrival) + " [" + self.status + "]"


class FlightOwner(models.Model):
	flight = models.ForeignKey(Flight, blank=True, null=True)
	owner = models.ForeignKey(User, blank=True, null=True)


class Reservation(models.Model):
	# foreign Keys
	flight = models.ForeignKey(Flight, on_delete=models.CASCADE)
	airline = models.ForeignKey(Airline, on_delete=models.CASCADE)
	client = models.ForeignKey(Client, on_delete=models.CASCADE, blank=True, default=None, null=True)

	# fields
	type = models.CharField(max_length=1, choices=FLIGHT_CLASS)
	seat = models.CharField(max_length=4, blank=True, null=True)
	price = models.DecimalField(max_digits=12, decimal_places=2, default=0)
	paid = models.BooleanField(default=False)
	checkin = models.BooleanField(default=False)
	created = models.DateTimeField(auto_now_add=True)
	forename = models.CharField(max_length=50, blank=True, null=True)
	surname = models.CharField(max_length=50, blank=True, null=True)

	def __str__(self):
		if self.seat:
			return self.flight.flight_number + ": " + self.airline.code + ". " + self.forename + " " + self.surname + " (" + self.seat + ")"
		return self.flight.flight_number + ": " + self.airline.code
