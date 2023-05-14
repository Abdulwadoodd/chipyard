package chipyard.example

import chisel3._
import chisel3.util._
import chisel3.experimental.{IntParam, BaseModule}
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.config.{Parameters, Field, Config}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.regmapper.{HasRegMap, RegField}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._

case class JustReadParams(
	address: BigInt = 0x2000,
	width: Int = 32)

case object JustReadKey extends Field[Option[JustReadParams]](None)

class JustReadIO(val w:Int) extends Bundle {
	val toBeRead = Output (UInt(w.W))
}

trait HasJustReadIO extends BaseModule {
	val w: Int
	val io = IO(new JustReadIO(w)) // <== instance of the above bundle
}

class JustReadMMIOChiselModule(val w: Int) extends Module
	with HasJustReadIO {
			
	val a = RegInit(230.U(w.W))

	io.toBeRead := a
}

trait JustReadTopIO extends Bundle {
	val isReady = Output(Bool())
}

trait JustReadTopModule extends HasRegMap
{
	val io: JustReadTopIO
	implicit val p: Parameters
	def params: JustReadParams

	val isJustRead = Reg(UInt(params.width.W))

	val impl = Module(new JustReadMMIOChiselModule(params.width))

	isJustRead := impl.io.toBeRead

	regmap(
		0x00 -> Seq(
			RegField.r(params.width, isJustRead)) // r is for read-only
	)
}

class JustReadTL(params: JustReadParams, beatBytes: Int)(implicit p: Parameters)
	extends TLRegisterRouter(
		params.address, "justread", Seq("abd,justreadit"),
		beatBytes = beatBytes)(
			new TLRegBundle(params, _) with JustReadTopIO)(
				new TLRegModule(params, _, _) with JustReadTopModule)

trait CanHavePeripheryJustReadModuleImp extends LazyModuleImp {
	val outer: CanHavePeripheryJustRead
}

trait CanHavePeripheryJustRead { this: BaseSubsystem =>
	private val portName = "justreadPortName"

	val justread = p(JustReadKey) match { 	
		case Some(params) => { 				
			val justread = LazyModule(new JustReadTL(params, pbus.beatBytes)(p))
			pbus.toVariableWidthSlave(Some(portName)) { justread.node } 
			// i to je ono sto povezujemo na RocketChip TL magistralu
			Some(justread)
		}
		case None => None
	}
}

class WithJustRead extends Config((site, here, up) => {
  case JustReadKey => Some(JustReadParams())
})