FILE="aircraft.java"
OUTPUT="aircraft_yarn.java"

sed 's/, new CubeDeformation(0.0F)//g' $FILE | sed 's/texOffs/uv/g' | sed 's/MeshDefinition/ModelData/g' | sed 's/PartDefinition/ModelPartData/g' | sed 's/LayerDefinition.create/TexturedModelData.of/g' | sed 's/addOrReplaceChild/addChild/g' | sed 's/addBox/cuboid/g' | sed 's/meshdefinition/modelData/g' | sed 's/partdefinition/modelPartData/g' | sed 's/pivotAndRotation/of/g' | sed 's/CubeListBuilder/ModelPartBuilder/g' | sed 's/PartPose.offset/ModelTransform.pivot/g' > $OUTPUT
