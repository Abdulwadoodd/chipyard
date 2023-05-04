package chipyard.fpga.arty

import chisel3._

import freechips.rocketchip.diplomacy.{LazyModule}
import org.chipsalliance.cde.config.{Parameters}

import sifive.fpgashells.shell.xilinx.artyshell.{ArtyShell}

import chipyard._
import chipyard.harness._
import chipyard.iobinders.{HasIOBinders}

class ArtyFPGATestHarness(override implicit val p: Parameters) extends ArtyShell with HasChipyardHarnessInstantiators {

  // Convert harness resets from Bool to Reset type.
  val hReset = Wire(Reset())
  hReset := ~ck_rst

  val dReset = Wire(AsyncReset())
  dReset := reset_core.asAsyncReset

  def success = {require(false, "Success not supported"); false.B }

  def implicitClock = clock_32MHz
  def implicitReset = hReset

  instantiateChipTops()
}
