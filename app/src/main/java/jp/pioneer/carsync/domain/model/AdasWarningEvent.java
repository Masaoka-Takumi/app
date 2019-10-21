package jp.pioneer.carsync.domain.model;

/**
 * ADAS警報イベント.
 */
public enum AdasWarningEvent {
    OFF_ROAD_LEFT_SOLID_EVENT,//1．	左実線逸脱
    OFF_ROAD_RIGHT_SOLID_EVENT,//2．	右実線逸脱
    OFF_ROAD_LEFT_DASH_EVENT,//3．	左破線逸脱
    OFF_ROAD_RIGHT_DASH_EVENT,//4．	右破線逸脱
    WANDERING_ACROSS_SOLID_LANE_EVENT,//12．	実線踏み警報(未使用)
    WANDERING_ACROSS_DASH_LANE_EVENT,//13．	破線踏み警報(未使用)
    FORWARD_TTC_COLLISION_EVENT,//5．	前方車両距離監視警報
    FORWARD_HEADWAY_COLLISION_EVENT,//6．	前方車両衝突警報
    VIRTUAL_BUMPER_COLLISION_EVENT,//7．	バーチャルバンパー衝突警報(未使用)
    FRONT_VEHICLE_MOVING_EVENT,//8．	前方車両移動事件警報(未使用)
    PEDESTRIAN_WARNING_EVENT,//9．	前方歩行者衝突警報
    PEDESTRIAN_CAREFUL_EVENT,//10．	前方歩行者横断警報
    PEDESTRIAN_SAFE_EVENT,//11．	前方歩行者安全事件警報(未使用)
    LANE_KEEP_WARNING_EVENT,//14．	LKW
    INVALID_PARAMETER_EVENT,//(未使用)
}
