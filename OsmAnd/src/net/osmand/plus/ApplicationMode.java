package net.osmand.plus;

import android.content.Context;

import android.support.annotation.DrawableRes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedHashSet;
import net.osmand.PlatformUtil;
import net.osmand.StateChangedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.osmand.plus.routing.RouteProvider.RouteService;
import net.osmand.util.Algorithms;
import org.apache.commons.logging.Log;


public class ApplicationMode {

	private static final Log LOG = PlatformUtil.getLog(ApplicationMode.class);
	private static Map<String, Set<ApplicationMode>> widgetsVisibilityMap = new LinkedHashMap<>();
	private static Map<String, Set<ApplicationMode>> widgetsAvailabilityMap = new LinkedHashMap<>();
	private static List<ApplicationMode> defaultValues = new ArrayList<>();
	private static List<ApplicationMode> values = new ArrayList<>();
	private static List<ApplicationMode> cachedFilteredValues = new ArrayList<>();
	/*
	 * DEFAULT("Browse map"), CAR("Car"), BICYCLE("Bicycle"), PEDESTRIAN("Pedestrian"); NAUTICAL("boat"); PUBLIC_TRANSPORT("Public transport"); AIRCRAFT("Aircraft")
	 */
	public static final ApplicationMode DEFAULT = create(R.string.app_mode_default, "default").speed(1.5f, 5).arrivalDistance(90).defLocation().
			icon(R.drawable.map_world_globe_dark, R.drawable.ic_world_globe_dark, "map_world_globe_dark").reg();

	public static final ApplicationMode CAR = create(R.string.app_mode_car, "car").speed(15.3f, 35).carLocation().
			icon(R.drawable.map_action_car_dark, R.drawable.ic_action_car_dark, "ic_action_car_dark").setRoutingProfile("car").reg();

	public static final ApplicationMode BICYCLE = create(R.string.app_mode_bicycle, "bicycle").speed(5.5f, 15).arrivalDistance(60).offRouteDistance(50).bicycleLocation().
			icon(R.drawable.map_action_bicycle_dark, R.drawable.ic_action_bicycle_dark, "ic_action_bicycle_dark").setRoutingProfile("bicycle").reg();

	public static final ApplicationMode PEDESTRIAN = create(R.string.app_mode_pedestrian, "pedestrian").speed(1.5f, 5).arrivalDistance(45).offRouteDistance(20).
			icon(R.drawable.map_action_pedestrian_dark, R.drawable.ic_action_pedestrian_dark, "ic_action_pedestrian_dark").setRoutingProfile("pedestrian").reg();

	public static final ApplicationMode PUBLIC_TRANSPORT = create(R.string.app_mode_public_transport, "public_transport").
			icon(R.drawable.map_action_bus_dark, R.drawable.ic_action_bus_dark, "ic_action_bus_dark").setRoutingProfile("public_transport").reg();

	public static final ApplicationMode BOAT = create(R.string.app_mode_boat, "boat").speed(5.5f, 20).nauticalLocation().
			icon(R.drawable.map_action_sail_boat_dark, R.drawable.ic_action_sail_boat_dark, "ic_action_sail_boat_dark").setRoutingProfile("boat").reg();

	public static final ApplicationMode AIRCRAFT = create(R.string.app_mode_aircraft, "aircraft").speed(40f, 100).carLocation().
			icon(R.drawable.map_action_aircraft, R.drawable.ic_action_aircraft, "ic_action_aircraft").setRouteService(RouteService.STRAIGHT).setRoutingProfile("STRAIGHT_LINE_MODE").reg();

	public static final ApplicationMode SKI = create(R.string.app_mode_skiing, "ski").speed(5.5f, 15).arrivalDistance(60).offRouteDistance(50).bicycleLocation().
		icon(R.drawable.ic_plugin_skimaps, R.drawable.ic_plugin_skimaps, "ic_plugin_skimaps").setRoutingProfile("ski").reg();

	static {
		ApplicationMode[] exceptDefault = new ApplicationMode[]{CAR, PEDESTRIAN, BICYCLE, BOAT, AIRCRAFT, PUBLIC_TRANSPORT, SKI};
		ApplicationMode[] exceptPedestrianAndDefault = new ApplicationMode[]{CAR, BICYCLE, BOAT, AIRCRAFT, PUBLIC_TRANSPORT, SKI};
		ApplicationMode[] exceptAirBoatDefault = new ApplicationMode[]{CAR, BICYCLE, PEDESTRIAN, SKI};
		ApplicationMode[] pedestrian = new ApplicationMode[]{PEDESTRIAN};
		ApplicationMode[] pedestrianBicycle = new ApplicationMode[]{PEDESTRIAN, BICYCLE};

		ApplicationMode[] all = null;
		ApplicationMode[] none = new ApplicationMode[]{};

		// left
		regWidgetVisibility("next_turn", exceptPedestrianAndDefault);
		regWidgetVisibility("next_turn_small", pedestrian);
		regWidgetVisibility("next_next_turn", exceptPedestrianAndDefault);
		regWidgetAvailability("next_turn", exceptDefault);
		regWidgetAvailability("next_turn_small", exceptDefault);
		regWidgetAvailability("next_next_turn", exceptDefault);

		// right
		regWidgetVisibility("intermediate_distance", all);
		regWidgetVisibility("distance", all);
		regWidgetVisibility("time", all);
		regWidgetVisibility("intermediate_time", all);
		regWidgetVisibility("speed", exceptPedestrianAndDefault);
		regWidgetVisibility("max_speed", CAR);
		regWidgetVisibility("altitude", pedestrianBicycle);
		regWidgetVisibility("gps_info", none);
		regWidgetAvailability("intermediate_distance", all);
		regWidgetAvailability("distance", all);
		regWidgetAvailability("time", all);
		regWidgetAvailability("intermediate_time", all);
		regWidgetAvailability("map_marker_1st", none);
		regWidgetAvailability("map_marker_2nd", none);

		// top
		regWidgetVisibility("config", none);
		regWidgetVisibility("layers", none);
		regWidgetVisibility("compass", none);
		regWidgetVisibility("street_name", exceptAirBoatDefault);
		regWidgetVisibility("back_to_location", all);
		regWidgetVisibility("monitoring_services", none);
		regWidgetVisibility("bgService", none);
	}

	public static class ApplicationModeBuilder {
		private ApplicationMode applicationMode;

		public ApplicationMode reg() {
			values.add(applicationMode);
			defaultValues.add(applicationMode);
			return applicationMode;
		}

		public ApplicationMode customReg() {
			values.add(applicationMode);
			return applicationMode;
		}

		public ApplicationModeBuilder icon(int mapIcon, int smallIconDark, String iconName) {
			applicationMode.mapIconId = mapIcon;
			applicationMode.smallIconDark = smallIconDark;
			applicationMode.iconName = iconName;
			return this;
		}

		public ApplicationModeBuilder parent(ApplicationMode parent) {
			applicationMode.parent = parent;
			String parentTypeName = parent.getStringKey();
			if (parentTypeName.equals("car") || parentTypeName.equals("aircraft")) {
				this.carLocation();
			} else if (parentTypeName.equals("bicycle")) {
				this.bicycleLocation();
			} else if (parentTypeName.equals("boat")) {
				this.nauticalLocation();
			} else {
				this.defLocation();
			}
			return this;
		}


		public ApplicationModeBuilder carLocation() {
			applicationMode.bearingIconDay = R.drawable.map_car_bearing;
			applicationMode.bearingIconNight = R.drawable.map_car_bearing_night;
			applicationMode.headingIconDay = R.drawable.map_car_location_view_angle;
			applicationMode.headingIconNight = R.drawable.map_car_location_view_angle_night;
			applicationMode.locationIconDay = R.drawable.map_car_location;
			applicationMode.locationIconNight = R.drawable.map_car_location_night;
			applicationMode.locationIconDayLost = R.drawable.map_car_location_lost;
			applicationMode.locationIconNightLost = R.drawable.map_car_location_lost_night;
			return this;
		}

		public ApplicationModeBuilder bicycleLocation() {
			applicationMode.bearingIconDay = R.drawable.map_bicycle_bearing;
			applicationMode.bearingIconNight = R.drawable.map_bicycle_bearing_night;
			applicationMode.headingIconDay = R.drawable.map_bicycle_location_view_angle;
			applicationMode.headingIconNight = R.drawable.map_bicycle_location_view_angle_night;
			applicationMode.locationIconDay = R.drawable.map_bicycle_location;
			applicationMode.locationIconNight = R.drawable.map_bicycle_location_night;
			applicationMode.locationIconDayLost = R.drawable.map_bicycle_location_lost;
			applicationMode.locationIconNightLost = R.drawable.map_bicycle_location_lost_night;
			return this;
		}

		public ApplicationModeBuilder defLocation() {
			applicationMode.bearingIconDay = R.drawable.map_pedestrian_bearing;
			applicationMode.bearingIconNight = R.drawable.map_pedestrian_bearing_night;
			applicationMode.headingIconDay = R.drawable.map_default_location_view_angle;
			applicationMode.headingIconNight = R.drawable.map_default_location_view_angle_night;
			applicationMode.locationIconDay = R.drawable.map_pedestrian_location;
			applicationMode.locationIconNight = R.drawable.map_pedestrian_location_night;
			applicationMode.locationIconDayLost = R.drawable.map_pedestrian_location_lost;
			applicationMode.locationIconNightLost = R.drawable.map_pedestrian_location_lost_night;
			return this;
		}

		public ApplicationModeBuilder nauticalLocation() {
			applicationMode.bearingIconDay = R.drawable.map_nautical_bearing;
			applicationMode.bearingIconNight = R.drawable.map_nautical_bearing_night;
			applicationMode.headingIconDay = R.drawable.map_nautical_location_view_angle;
			applicationMode.headingIconNight = R.drawable.map_nautical_location_view_angle_night;
			applicationMode.locationIconDay = R.drawable.map_nautical_location;
			applicationMode.locationIconNight = R.drawable.map_nautical_location_night;
			return this;
		}

		public ApplicationModeBuilder speed(float defSpeed, int distForTurn) {
			applicationMode.defaultSpeed = defSpeed;
			applicationMode.minDistanceForTurn = distForTurn;
			return this;
		}

		public ApplicationModeBuilder arrivalDistance(int arrivalDistance) {
			applicationMode.arrivalDistance = arrivalDistance;
			return this;
		}

		public ApplicationModeBuilder offRouteDistance(int offRouteDistance) {
			applicationMode.offRouteDistance = offRouteDistance;
			return this;
		}

		public ApplicationModeBuilder userProfileTitle(String userProfileTitle) {
			applicationMode.userProfileName = userProfileTitle;
			return this;
		}

		public ApplicationModeBuilder setRoutingProfile(String routingProfileName) {
			applicationMode.routingProfile = routingProfileName;
			return this;
		}

		public ApplicationModeBuilder setRouteService(RouteService service) {
			applicationMode.routeService = service;
			return this;
		}
	}

	private static ApplicationModeBuilder create(int key, String stringKey) {
		ApplicationModeBuilder builder = new ApplicationModeBuilder();
		builder.applicationMode = new ApplicationMode(key, stringKey);
		return builder;
	}

	public static ApplicationModeBuilder createCustomMode(String userProfileTitle, String stringKey) {
		return create(-1, stringKey).userProfileTitle(userProfileTitle);
	}

	private ApplicationMode(int key, String stringKey) {
		this.key = key;
		this.stringKey = stringKey;
	}

	public static List<ApplicationMode> values(OsmandApplication app) {
		if (customizationListener == null) {
			customizationListener = new OsmAndAppCustomization.OsmAndAppCustomizationListener() {
				@Override
				public void onOsmAndSettingsCustomized() {
					cachedFilteredValues = new ArrayList<>();
				}
			};
			app.getAppCustomization().addListener(customizationListener);
		}
		if (cachedFilteredValues.isEmpty()) {
			OsmandSettings settings = app.getSettings();
			if (listener == null) {
				listener = new StateChangedListener<String>() {
					@Override
					public void stateChanged(String change) {
						cachedFilteredValues = new ArrayList<>();
					}
				};
				settings.AVAILABLE_APP_MODES.addListener(listener);
			}
			String available = settings.AVAILABLE_APP_MODES.get();
			cachedFilteredValues = new ArrayList<>();
			for (ApplicationMode v : values) {
				if (available.indexOf(v.getStringKey() + ",") != -1 || v == DEFAULT) {
					cachedFilteredValues.add(v);
				}
			}
		}
		return cachedFilteredValues;
	}

	public static List<ApplicationMode> allPossibleValues() {
		return new ArrayList<>(values);
	}

	public static List<ApplicationMode> getDefaultValues() {
		return new ArrayList<>(defaultValues);
	}

	// returns modifiable ! Set<ApplicationMode> to exclude non-wanted derived
	public static Set<ApplicationMode> regWidgetVisibility(String widgetId, ApplicationMode... am) {
		HashSet<ApplicationMode> set = new HashSet<>();
		if (am == null) {
			set.addAll(values);
		} else {
			Collections.addAll(set, am);
		}
		for (ApplicationMode m : values) {
			// add derived modes
			if (set.contains(m.getParent())) {
				set.add(m);
			}
		}
		widgetsVisibilityMap.put(widgetId, set);
		return set;
	}

	public boolean isWidgetCollapsible(String key) {
		return false;
	}

	public boolean isWidgetVisible(OsmandApplication app, String key) {
		if (app.getAppCustomization().areWidgetsCustomized()) {
			return app.getAppCustomization().isWidgetVisible(key, this);
		}
		Set<ApplicationMode> set = widgetsVisibilityMap.get(key);
		if (set == null) {
			return false;
		}
		return set.contains(this);
	}

	public static Set<ApplicationMode> regWidgetAvailability(String widgetId, ApplicationMode... am) {
		HashSet<ApplicationMode> set = new HashSet<>();
		if (am == null) {
			set.addAll(values);
		} else {
			Collections.addAll(set, am);
		}
		for (ApplicationMode m : values) {
			// add derived modes
			if (set.contains(m.getParent())) {
				set.add(m);
			}
		}
		widgetsAvailabilityMap.put(widgetId, set);
		return set;
	}

	public boolean isWidgetAvailable(OsmandApplication app, String key) {
		if (app.getAppCustomization().areWidgetsCustomized()) {
			return app.getAppCustomization().isWidgetAvailable(key, this);
		}
		Set<ApplicationMode> set = widgetsAvailabilityMap.get(key);
		if (set == null) {
			return true;
		}
		return set.contains(this);
	}

	public static List<ApplicationMode> getModesDerivedFrom(ApplicationMode am) {
		List<ApplicationMode> list = new ArrayList<ApplicationMode>();
		for (ApplicationMode a : values) {
			if (a == am || a.getParent() == am) {
				list.add(a);
			}
		}
		return list;
	}

	public ApplicationMode getParent() {
		return parent;
	}

	public int getSmallIconDark() {
		return smallIconDark;
	}

	public boolean hasFastSpeed() {
		return getDefaultSpeed() > 10;
	}

	public int getResourceBearingDay() {
		return bearingIconDay;
	}

	public int getResourceBearingNight() {
		//return bearingIconDay;
		return bearingIconNight;
	}

	public int getResourceHeadingDay() {
		return headingIconDay;
	}

	public int getResourceHeadingNight() {
		return headingIconNight;
	}

	public int getResourceLocationDay() {
		return locationIconDay;
	}

	public int getResourceLocationNight() {
		//return locationIconDay;
		return locationIconNight;
	}

	public int getResourceLocationDayLost() {
		return locationIconDayLost;
	}

	public int getResourceLocationNightLost() {
		return locationIconNightLost;
	}

	public String getStringKey() {
		return stringKey;
	}

	public int getMapIconId() {
		return mapIconId;
	}

	public int getStringResource() {
		return key;
	}

	public String toHumanString(Context ctx) {
		if (Algorithms.isEmpty(userProfileName) && key != -1) {
			return ctx.getString(key);
		} else {
			return userProfileName;
		}

	}

	public String toHumanStringCtx(Context ctx) {
		if (Algorithms.isEmpty(userProfileName)) {
			return ctx.getString(key);
		} else {
			return userProfileName;
		}
	}

	public RouteService getRouteService() {
		return routeService;
	}

	public static ApplicationMode valueOfStringKey(String key, ApplicationMode def) {
		for (ApplicationMode p : values) {
			if (p.getStringKey().equals(key)) {
				return p;
			}
		}
		return def;
	}

	public String getIconName() {
		return iconName;
	}

	public float getDefaultSpeed() {
		return defaultSpeed;
	}

	public int getMinDistanceForTurn() {
		return minDistanceForTurn;
	}

	public int getArrivalDistance() {
		return arrivalDistance;
	}

	public int getOffRouteDistance() {
		return offRouteDistance;
	}

	public boolean isDerivedRoutingFrom(ApplicationMode mode) {
		return this == mode || getParent() == mode;
	}

	public String getRoutingProfile() {
		return routingProfile;
	}

	public String getUserProfileName() {
		return userProfileName;
	}

	public float getUserDefaultSpeed() {
		return userDefaultSpeed;
	}

	@Expose private final int key;
	@Expose private final String stringKey;
	@Expose private String userProfileName;
	@Expose private ApplicationMode parent;
	@Expose private String iconName = "map_world_globe_dark";
	@Expose private int mapIconId = R.drawable.map_world_globe_dark;
	@Expose private int smallIconDark = R.drawable.ic_world_globe_dark;
	@Expose private float defaultSpeed = 10f;
	@Expose private float userDefaultSpeed = 2.777777778f;
	@Expose private int minDistanceForTurn = 50;
	@Expose private int arrivalDistance = 90;
	@Expose private int offRouteDistance = 350;
	@Expose private int bearingIconDay = R.drawable.map_pedestrian_bearing;
	@Expose private int bearingIconNight = R.drawable.map_pedestrian_bearing_night;
	@Expose private int headingIconDay = R.drawable.map_pedestrian_location_view_angle;
	@Expose private int headingIconNight = R.drawable.map_pedestrian_location_view_angle_night;
	@Expose private int locationIconDay = R.drawable.map_pedestrian_location;
	@Expose private int locationIconNight = R.drawable.map_pedestrian_location_night;
	@Expose private int locationIconDayLost = R.drawable.map_pedestrian_location_lost;
	@Expose private int locationIconNightLost = R.drawable.map_pedestrian_location_lost_night;
	@Expose private String routingProfile = null;
	@Expose private RouteService routeService = RouteService.OSMAND;
	private static StateChangedListener<String> listener;
	private static OsmAndAppCustomization.OsmAndAppCustomizationListener customizationListener;

	public static void saveCustomModeToSettings(OsmandSettings settings){
		List<ApplicationMode> customModes = new ArrayList<>();
		for (ApplicationMode mode : values) {
			if (mode.parent != null) {
				customModes.add(mode);
			}
		}
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		String profiles = gson.toJson(customModes);
		settings.CUSTOM_APP_PROFILES.set(profiles);
	}

	public static void initCustomModes(OsmandSettings settings){
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		Type t = new TypeToken<ArrayList<ApplicationMode>>() {}.getType();
		List<ApplicationMode> customProfiles = gson.fromJson(settings.CUSTOM_APP_PROFILES.get(), t);

		if (!Algorithms.isEmpty(customProfiles)) {
			for (ApplicationMode m : customProfiles) {
				if (!values.contains(m)) {
					values.add(m);
				}
			}
		}
	}

	public static void deleteCustomMode(String userModeTitle, OsmandApplication app) {
		Iterator<ApplicationMode> it = values.iterator();
		while (it.hasNext()) {
			ApplicationMode m = it.next();
			if (m.userProfileName != null && m.userProfileName.equals(userModeTitle)) {
				it.remove();
			}
		}
		ApplicationMode.saveCustomModeToSettings(app.getSettings());
	}

	public static boolean changeProfileStatus(ApplicationMode mode, boolean isSelected, OsmandApplication app) {
		Set<ApplicationMode> selectedModes = new LinkedHashSet<>(ApplicationMode.values(app));
		StringBuilder vls = new StringBuilder(ApplicationMode.DEFAULT.getStringKey() + ",");
		if (allPossibleValues().contains(mode)) {
			if (isSelected) {
				selectedModes.add(mode);
			} else {
				selectedModes.remove(mode);
				if (app.getSettings().APPLICATION_MODE.get() == mode) {
					app.getSettings().APPLICATION_MODE.set(ApplicationMode.DEFAULT);
				}
			}
			for (ApplicationMode m : selectedModes) {
				vls.append(m.getStringKey()).append(",");
			}
			app.getSettings().AVAILABLE_APP_MODES.set(vls.toString());
			return true;
		}
		return false;
	}

	@DrawableRes public int getIconRes(Context app) {
		try {
			return app.getResources().getIdentifier(iconName, "drawable", app.getPackageName());
		} catch (Exception e) {
			return R.drawable.map_world_globe_dark;
		}
	}
}