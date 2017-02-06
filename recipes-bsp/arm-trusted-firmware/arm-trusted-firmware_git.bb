DESCRIPTION = "ARM Trusted Firmware"

LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://license.md;md5=829bdeb34c1d9044f393d5a16c068371"

PROVIDES = "virtual/arm-trusted-firmware"

inherit deploy

DEPENDS += "u-boot-mkimage-native"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

BRANCH ?= "master"
REPO ?= "git://gitenterprise.xilinx.com/Linux/arm-trusted-firmware.git"
SRCREV ?= "a7a49d7f6373633d3a6c45929bc8105e91b182fe"

BRANCHARG = "${@['nobranch=1', 'branch=${BRANCH}'][d.getVar('BRANCH', True) != '']}"
SRC_URI = "${REPO};protocol=https;${BRANCHARG}"

XILINX_RELEASE_VERSION = "v2017.1"

PV = "1.3-xilinx-${XILINX_RELEASE_VERSION}+git${SRCPV}"

COMPATIBLE_MACHINE = "zynqmp"
PLATFORM_zynqmp = "zynqmp"

# requires CROSS_COMPILE set by hand as there is no configure script
export CROSS_COMPILE="${TARGET_PREFIX}"

# Let the Makefile handle setting up the CFLAGS and LDFLAGS as it is a standalone application
CFLAGS[unexport] = "1"
LDFLAGS[unexport] = "1"
AS[unexport] = "1"
LD[unexport] = "1"

do_configure() {
	:
}

do_compile() {
	oe_runmake -C ${S} BUILD_BASE=${B} PLAT=${PLATFORM} RESET_TO_BL31=1 bl31
}

do_install() {
	:
}

OUTPUT_DIR = "${B}/${PLATFORM}/release"

do_deploy() {
	install -d ${DEPLOYDIR}
	install -m 0644 ${OUTPUT_DIR}/bl31/bl31.elf ${DEPLOYDIR}/arm-trusted-firmware.elf
	install -m 0644 ${OUTPUT_DIR}/bl31.bin ${DEPLOYDIR}/arm-trusted-firmware.bin

	# Get the entry point address from the elf.
	BL31_BASE_ADDR=$(${READELF} -h ${OUTPUT_DIR}/bl31/bl31.elf | egrep -m 1 -i "entry point.*?0x" | sed -r 's/.*?(0x.*?)/\1/g')
	mkimage -A arm64 -O linux -T kernel -C none \
		-a $BL31_BASE_ADDR -e $BL31_BASE_ADDR \
		-d ${OUTPUT_DIR}/bl31.bin ${DEPLOYDIR}/arm-trusted-firmware.ub
}
addtask deploy before do_build after do_compile
