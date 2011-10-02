package geo;

import gl.Color;
import gl.GLCamera;
import gl.GLFactory;
import gl.MeshComponent;
import gl.animations.AnimationColorMorph;
import gl.animations.AnimationMove;
import gl.animations.AnimationRotate;
import util.EfficientList;
import util.Vec;
import worldData.Obj;
import android.util.Log;

import components.Entity;
import components.ProximitySensor;

public class DefaultNodeEdgeListener implements NodeListener, EdgeListener {

	private static final String LOG_TAG = "NodeListener";
	private static final float MIN_DISTANCE = 20;
	private static final float BLENDING_TIME = 4;
	private static final float MOVEMENT_TIME = 3;
	private GLCamera camera;
	private MeshComponent nodeMesh;

	public DefaultNodeEdgeListener(GLCamera glCamera) {
		camera = glCamera;
		nodeMesh = GLFactory.getInstance().newDiamond(null);
	}

	private Color newNormalColor() {
		return Color.blue();
	}

	private Color newHighlightColor() {
		return Color.red();
	}

	private Color newNotJetWalkedColor() {
		return Color.blackTransparent();
	}

	private Color newPathHighlightColor() {
		return new Color(0, 1, 0, 0.7f);
	}

	private Color newAlreadyPassedColor() {
		return new Color(0, 1, 0, 0);
	}

	@Override
	public boolean addFirstNodeToGraph(final GeoGraph targetGraph,
			GeoObj newNode) {
		newNode.setComp(newNodeMesh());
		setNormalTransformations(newNode.getGraphicsComponent());

		Log.d(LOG_TAG, "First node will be added now..");

		Log.d(LOG_TAG, "Adding obj " + newNode
				+ " to graph with number of nodes="
				+ targetGraph.getNodes().myLength);

		Log.d(LOG_TAG, "Setting special props for first node.");
		setHighlightNodeTransformations(newNode.getGraphicsComponent());
		newNode.setComp(newProxiSensor(targetGraph));

		return targetGraph.add(newNode);

	}

	@Override
	public boolean addNodeToGraph(final GeoGraph targetGraph, GeoObj newNode) {
		newNode.setComp(newNodeMesh());

		/*
		 * its a geoObj so the diamond will automatically surrounded by a
		 * mesh-group. change the color of this group:
		 */
		setNormalTransformations(newNode.getGraphicsComponent());

		Log.d(LOG_TAG, "Adding obj " + newNode
				+ " to graph with number of nodes="
				+ targetGraph.getNodes().myLength);

		return targetGraph.add(newNode);
	}

	@Override
	public boolean addLastNodeToGraph(GeoGraph graph, GeoObj objectToAdd) {
		return addNodeToGraph(graph, objectToAdd);
	}

	private Entity newNodeMesh() {

		return nodeMesh;
	}

	private void setNormalTransformations(MeshComponent m) {
		m.myColor = newNormalColor();
	}

	private void setHighlightNodeTransformations(MeshComponent m) {
		if (m != null) {
			m.myAnimation = new AnimationColorMorph(BLENDING_TIME,
					newHighlightColor());
			m.addAnimation(new AnimationRotate(50, new Vec(0, 0, 1)));
		}
	}

	private void setHighlightEdgeTransformation(GeoObj edge) {
		if (edge != null) {
			edge.getGraphicsComponent().myAnimation = new AnimationColorMorph(
					BLENDING_TIME, newPathHighlightColor());
		}

	}

	private void setPassedTransformationsOn(MeshComponent m) {
		m.myAnimation = new AnimationColorMorph(BLENDING_TIME,
				newAlreadyPassedColor());
		m.addAnimation(new AnimationMove(MOVEMENT_TIME, new Vec(0, 0, -2)));

	}

	private Entity newProxiSensor(final GeoGraph targetGraph) {
		return new ProximitySensor(camera, MIN_DISTANCE) {

			@Override
			public void onObjectIsCloseToCamera(GLCamera glCamera, Obj obj,
					MeshComponent meshComp, float currentDistance) {
				Log.d(LOG_TAG, "Proxim Sensor executed, close to " + obj);

				Log.d(LOG_TAG, "     meshComp=" + meshComp);
				Log.d(LOG_TAG, "     meshComp.myColor=" + meshComp.myColor);

				setPassedTransformationsOn(meshComp);
				obj.remove(this);

				if (obj instanceof GeoObj)
					setToNextWayPoint(targetGraph, (GeoObj) obj);

			}

		};
	}

	private void setToNextWayPoint(GeoGraph graph, GeoObj justCheckedNode) {

		EfficientList<GeoObj> followers = graph
				.getFollowingNodesOf(justCheckedNode);
		if (followers != null) {
			for (int i = 0; i < followers.myLength; i++) {
				GeoObj followingNode = followers.get(i);
				followingNode.setComp(newProxiSensor(graph));
				setHighlightNodeTransformations(followingNode
						.getGraphicsComponent());
				setHighlightEdgeTransformation(graph.getEdge(justCheckedNode,
						followingNode));
			}
		} else {
			Log.d(LOG_TAG, justCheckedNode + " has no following nodes");
		}

	}

	@Override
	public void addEdgeToGraph(GeoGraph targetGraph, GeoObj startPoint,
			GeoObj endPoint) {

		// add an edge:
		if (startPoint != null) {
			Log.d(LOG_TAG, "Adding edge from " + startPoint + " to " + endPoint);
			GeoObj edge = targetGraph.addEdge(startPoint, endPoint, Edge
					.getDefaultMesh(targetGraph, startPoint, endPoint, null));
			edge.getGraphicsComponent().myColor = newNotJetWalkedColor();
		}

	}

}