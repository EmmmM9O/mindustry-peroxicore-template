package peroxicore

import arc.*
import arc.math.geom.*
import arc.struct.*
import mindustry.*
import mindustry.content.*
import mindustry.ctype.*
import mindustry.game.*
import mindustry.gen.*
import mindustry.mod.*
import peroxicore.annotations.*
import peroxicore.ponder.*
import peroxicore.ponder.api.*
import peroxicore.ponder.api.dsl.*
import peroxicore.ponder.scene.*

@ModConfig(
  name = "peroxicore-template",
  displayName = "PeroxiCore Template",
  author = "Stellarcus",
  version = "1.0",
  dependencies = ["peroxicore", "peroxicore-ponder"]
)
@ImportPeroxiCore
class PeroxiCoreTemplate : Mod() {
  init {
    Events.on(EventType.ClientLoadEvent::class.java) {
      initPonder()
    }
  }

  fun initPonder() {
    Blocks.duo.register {
      keyframes {
        speed(1f)
        world(13, 11) {
          fillFloor(Blocks.stone)
          (8 at 5).focus(5)
        }
        frame(50f) {
          Weathers.rain.create()
          (8 at 5).build(Blocks.duo)
          (9 at 5).label("@block.duo.name", 4f)
        }
      }
    }
  }

  override fun init() {
  }

  override fun loadContent() {}
}
